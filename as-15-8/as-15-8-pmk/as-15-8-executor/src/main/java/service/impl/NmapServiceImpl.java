package service.impl;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import execution.NmapExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmap4j.core.flags.Flag;
import nmap4j.core.nmap.ExecutionResults;
import nmap4j.core.scans.BaseScan;
import nmap4j.core.scans.IScan;
import nmap4j.data.NMapRun;
import nmap4j.data.host.ports.Port;
import nmap4j.parser.OnePassParser;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Service;
import proxychains.ProxychainsConfigurator;
import robots.exceptions.ExecutionException;
import service.CheckUnitVerificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NmapServiceImpl implements CheckUnitVerificationService {

    private boolean isRunning = false;

    private final ExecutorProperties executorProperties;

    @Override
    public Map<AccessToolUnit, List<CheckUnitType>> getSupportedTypes() {
        return new HashMap<AccessToolUnit, List<CheckUnitType>>(){{
            put(AccessToolUnit.PROXY, Arrays.asList(CheckUnitType.IP_V4, CheckUnitType.IP_V4_SUBNET, CheckUnitType.IP_V6, CheckUnitType.IP_V6_SUBNET));
            put(AccessToolUnit.VPN, Arrays.asList(CheckUnitType.IP_V4, CheckUnitType.IP_V4_SUBNET, CheckUnitType.IP_V6, CheckUnitType.IP_V6_SUBNET));
        }};
    }

    @Override
    public ExecutionJobResult run(CheckUnitJob checkUnitJob) throws ExecutionException {
        try {
            checkJob(checkUnitJob);
            ExecutorProperties.NmapProperties nmapProperties = executorProperties.getNmap();
            String verificationName = "jobID = " + checkUnitJob.getJobID() +
                    " accessTool = " + checkUnitJob.getAccessTool() +
                    " checkUnit = " + checkUnitJob.getCheckUnit().getValue();
           /* if(!this.isRunning)
                throw new ExecutionException("Ошибка при запуске проверки запрещенного ресурса. Сервис проверки остановлен!");*/
            log.info("Запуск проверки nmap: " + verificationName);

            ProxychainsConfigurator proxychainsConfigurator = null;
            Path outputFile = Files.createTempFile("job_" + checkUnitJob.getJobID() + "_output", ".xml");
            try {
                if(nmapProperties.getUseProxy())
                    proxychainsConfigurator = createProxychainsConfigurator(checkUnitJob.getAccessTool());
                BaseScan baseScan;
                if(proxychainsConfigurator != null)
                    baseScan = new BaseScan(nmapProperties.getPath(), proxychainsConfigurator.getConfigFile());
                else
                    baseScan = new BaseScan(nmapProperties.getPath());

                baseScan.includeHost(checkUnitJob.getCheckUnit().getValue());
                baseScan.addPorts(Arrays.stream(nmapProperties.getPortsToCheck()).mapToInt(Integer::parseInt).toArray());
                baseScan.addFlag(Flag.TREAT_HOSTS_AS_ONLINE);
                baseScan.addFlag(Flag.CONNECT_SCAN);
                if(checkUnitJob.getCheckUnit().getType().equals(CheckUnitType.IP_V6) ||
                        checkUnitJob.getCheckUnit().getType().equals(CheckUnitType.IP_V6_SUBNET)){
                    baseScan.addFlag(Flag.IPV6);
                }
                baseScan.setOutputType(IScan.OutputType.XML, outputFile.toAbsolutePath().toString());

                ExecutionResults results = baseScan.executeScan();

                log.info("Job: " + checkUnitJob.getJobID() + ". Nmap запущен командой: " + results.getExecutedCommand());
                log.info("Job: " + checkUnitJob.getJobID() + ". Ответ nmap: " + results.getOutput());
                log.info("Job: " + checkUnitJob.getJobID() + ". Результат nmap: " + new String(Files.readAllBytes(outputFile)));

                NMapRun nmapRun = parseNmapResult(outputFile);

                NmapExecutionResult nmapExecutionResult = new NmapExecutionResult();
                nmapExecutionResult.setCheckUnit(checkUnitJob.getCheckUnit());
                nmapExecutionResult.setAccessTool(checkUnitJob.getAccessTool());
                nmapExecutionResult.setNmapLog(results.getOutput());

                nmapRun.getHosts().forEach(host -> {
                    if (host.getAddresses().size() > 0) {
                        boolean isHostAvailable = false;
                        for(Port port : host.getPorts().getPorts()) {
                            if (port != null) {
                                String portState = port.getState().getState().toLowerCase();
                                if (!portState.equals("filtered")) {
                                    isHostAvailable = true;
                                    break;
                                }
                            }
                        }
                        if(isHostAvailable)
                            nmapExecutionResult.getAvailableHosts().add(host.getAddresses().get(0).getAddr());
                    }
                });

                return nmapExecutionResult;
            } finally {
                if(proxychainsConfigurator != null)
                    proxychainsConfigurator.close();
                if(outputFile != null && outputFile.toFile().exists())
                    if(!outputFile.toFile().delete())
                        log.warn("Ошибка удаления файла с результатом работы nmap. Job: "+checkUnitJob.getJobID());
            }
        } catch (Exception ex){
            if(ex instanceof ExecutionException)
                throw (ExecutionException) ex;
            else
                throw new ExecutionException("Job: " + checkUnitJob.getJobID() + ". Ошибка при проверке запрещенных ресуросов в nmap", ex);
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

    private void checkJob(CheckUnitJob checkUnitJob) throws ExecutionException {
        AccessToolUnit accessToolUnit = executorProperties.getAccessToolUnit(checkUnitJob.getAccessTool())
                .orElseThrow(() ->
                        new ExecutionException("Ошибка получения сервиса для выполнения проверки. ПС/ПАСД не определен в системе: " + checkUnitJob.getAccessTool()));
        if(!accessToolUnit.equals(AccessToolUnit.VPN) && !accessToolUnit.equals(AccessToolUnit.PROXY))
            throw new ExecutionException("Ошибка! ПС/ПАСД " + checkUnitJob.getAccessTool() + " не поддерживает проверку с помощью NMAP");
    }

    private ProxychainsConfigurator createProxychainsConfigurator(String accessTool) throws IOException, ExecutionException {
        AccessToolUnit accessToolUnit = executorProperties.getAccessToolUnit(accessTool)
                .orElseThrow(() -> new RuntimeException("Ошибка при получении скрипта для робота "+accessTool));
        Map<AccessToolParameter, String> robotProps = executorProperties.getProps().getAccessToolUnits()
                .get(accessToolUnit).getRobotProps()
                .get(accessTool).getProps();
        String proxyType = robotProps.get(AccessToolParameter.PROXY_TYPE);
        String proxyDns = robotProps.get(AccessToolParameter.PROXY_DNS_NAME);
        String proxyPort = robotProps.get(AccessToolParameter.PROXY_PORT);
        if(Strings.isNotEmpty(proxyDns)){
            return new ProxychainsConfigurator(
                    Strings.isNotEmpty(proxyType) ?
                            proxyType.toLowerCase().trim() :
                            "http",
                    proxyDns,
                    proxyPort
            );
        }
        throw new ExecutionException("Ошибка создания конфигуратора proxychains. Не задан адрес прокси сервера");
    }

    private NMapRun parseNmapResult(Path outputFile) {
        OnePassParser opp = new OnePassParser();
        log.info("Парсинг результата: " + outputFile.toAbsolutePath().toString()+". Class id: " + System.identityHashCode(this));
        NMapRun nmapRun = opp.parse(outputFile.toAbsolutePath().toString(), OnePassParser.FILE_NAME_INPUT);
        log.info("Результат разобран: " + outputFile.toAbsolutePath().toString()+". Class id: " + System.identityHashCode(this));
        if (nmapRun == null)
            throw new NullPointerException("Ошибка парсинга результата. Результат пустой.");
        return nmapRun;
    }
}
