package service.impl;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.NmapExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmap4j.core.flags.Flag;
import nmap4j.core.nmap.ExecutionResults;
import nmap4j.core.proxychains.ProxychainsConfigurator;
import nmap4j.core.scans.BaseScan;
import nmap4j.core.scans.IScan;
import nmap4j.data.NMapRun;
import nmap4j.parser.OnePassParser;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Service;
import robots.exceptions.ExecutionException;
import service.CheckUnitVerificationService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NmapServiceImpl implements CheckUnitVerificationService {

    private boolean isRunning = false;

    @Value("${nmap.path}")
    private String nmapPath;

    @Value("#{new Boolean('${nmap.use-proxy}')}")
    private Boolean useProxy;

    @Value("${nmap.ports-to-check}")
    private String[] portsToCheck;

    @Override
    public List<CheckUnitType> getCheckUnitTypes(){
        return Arrays.asList(CheckUnitType.IP_V4, CheckUnitType.IP_V4_SUBNET);
    }

    @Override
    public ExecutionJobResult run(CheckUnitJob checkUnitJob) throws ExecutionException {
        try {
            String verificationName = "jobID = " + checkUnitJob.getJobID() +
                    " accessTool = " + checkUnitJob.getAccessToolUnit() +
                    " checkUnit = " + checkUnitJob.getCheckUnit().getValue();
            if(!this.isRunning)
                throw new ExecutionException("Ошибка при запуске проверки запрещенного ресурса. Сервис проверки остановлен!");
            log.info("Запуск проверки nmap: " + verificationName);

            configureProxy(checkUnitJob);

            BaseScan baseScan = new BaseScan(nmapPath, useProxy);

            baseScan.includeHost(checkUnitJob.getCheckUnit().getValue());
            baseScan.addPorts(Arrays.stream(portsToCheck).mapToInt(Integer::parseInt).toArray());
            baseScan.addFlag(Flag.TREAT_HOSTS_AS_ONLINE);
            baseScan.setOutputType(IScan.OutputType.XML, "output.xml");

            ExecutionResults results = baseScan.executeScan();
            log.info("Nmap запущен командой: " + results.getExecutedCommand());
            log.info("Ответ nmap: " + results.getOutput());

            OnePassParser opp = new OnePassParser();
            NMapRun nmapRun = opp.parse(baseScan.getArgumentProperties().getFlagMap().get(Flag.XML_OUTPUT), OnePassParser.FILE_NAME_INPUT);

            if(nmapRun == null)
                throw new ExecutionException("Ошибка при проверке ресурса через nmap. Ошибка сохранения результата");

            NmapExecutionResult nmapExecutionResult = new NmapExecutionResult();
            nmapExecutionResult.setJobID(checkUnitJob.getJobID());
            nmapExecutionResult.setCheckUnit(checkUnitJob.getCheckUnit());
            nmapExecutionResult.setAccessToolUnit(checkUnitJob.getAccessToolUnit());
            nmapExecutionResult.setNmapLogs(results.getOutput());

            nmapRun.getHosts().forEach(host -> {
                if(host.getAddresses().size() > 0) {
                    Set<Long> openedPorts = new HashSet<>();
                    host.getPorts().getPorts().forEach(port -> {
                        if (port != null) {
                            if (port.getState().getState().toLowerCase().equals("open")) {
                                openedPorts.add(port.getPortId());
                            }
                        }
                    });
                    nmapExecutionResult.getOpenedPorts().put(host.getAddresses().get(0).getAddr(), openedPorts);
                }
            });

            return nmapExecutionResult;
        } catch (Exception ex){
            log.error("Nmap завершил работу с ошибкой: " + "jobID = " + checkUnitJob.getJobID() +
                    " accessTool = " + checkUnitJob.getAccessToolUnit() +
                    " checkUnit = " + checkUnitJob.getCheckUnit().getValue(), ex);
            throw new ExecutionException("Ошибка при проверке запрещенных ресуросов в nmap", ex);
        }
    }

    @Override
    public void start() {
        this.isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getPhase() {
        return AbstractMessageListenerContainer.DEFAULT_PHASE - 9;
    }

    private void configureProxy(CheckUnitJob checkUnitJob) throws IOException {
        String proxyType = checkUnitJob.getAccessToolParameters().get(AccessToolParameters.PROXY_TYPE);
        String proxyDns = checkUnitJob.getAccessToolParameters().get(AccessToolParameters.PROXY_DNS_NAME);
        if(Strings.isNotEmpty(proxyDns)){
            new ProxychainsConfigurator(
                    Strings.isNotEmpty(proxyType) ?
                            proxyType.toLowerCase().trim() :
                            "http",
                    proxyDns,
                    checkUnitJob.getAccessToolParameters().get(AccessToolParameters.PROXY_PORT)
            ).configure();
        }
    }
}
