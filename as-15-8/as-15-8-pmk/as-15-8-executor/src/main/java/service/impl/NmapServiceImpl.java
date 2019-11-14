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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NmapServiceImpl implements CheckUnitVerificationService {

    private boolean isRunning = false;

    private final ExecutorProperties executorProperties;

    @Override
    public List<CheckUnitType> getCheckUnitTypes(){
        return Arrays.asList(CheckUnitType.IP_V4, CheckUnitType.IP_V4_SUBNET);
    }

    @Override
    public ExecutionJobResult run(CheckUnitJob checkUnitJob) throws ExecutionException {
        try {
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
                baseScan.setOutputType(IScan.OutputType.XML, outputFile.toAbsolutePath().toString());

                log.info("Установлен файл для записи результата: "+checkUnitJob.getJobID()+", Файл: "+outputFile.toAbsolutePath().toString());
                ExecutionResults results = baseScan.executeScan();
                log.info("Job: " + checkUnitJob.getJobID() + ". Nmap запущен командой: " + results.getExecutedCommand());
                log.info("Job: " + checkUnitJob.getJobID() + ". Ответ nmap: " + results.getOutput());

                OnePassParser opp = new OnePassParser();
                log.info("Парсинг результата: "+checkUnitJob.getJobID()+", Файл: "+outputFile.toAbsolutePath().toString());
                NMapRun nmapRun = opp.parse(outputFile.toAbsolutePath().toString(), OnePassParser.FILE_NAME_INPUT);

                if (nmapRun == null)
                    throw new ExecutionException("Job: " + checkUnitJob.getJobID() + ". Ошибка при проверке ресурса через nmap. Ошибка парсинга результата");

                NmapExecutionResult nmapExecutionResult = new NmapExecutionResult();
                nmapExecutionResult.setJobID(checkUnitJob.getJobID());
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
}
