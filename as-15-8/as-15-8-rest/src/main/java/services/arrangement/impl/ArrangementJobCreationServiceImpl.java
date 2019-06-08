package services.arrangement.impl;

import enums.AccessToolParameters;
import exceptions.AS_15_8_Exception;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import model.catalog.*;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.stereotype.Service;
import repositories.ArrangementItemRepository;
import repositories.GlobalParametersRepository;
import services.arrangement.ArrangementJobCreationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 * Сервис создания сообщений на проверку ЕРДИ по мероприятию
 */

@Service
public class ArrangementJobCreationServiceImpl implements ArrangementJobCreationService {

    private ArrangementItemRepository arrangementItemRepo;
    private GlobalParametersRepository globalParametersRepo;

    public ArrangementJobCreationServiceImpl(ArrangementItemRepository arrangementItemRepo,
                                             GlobalParametersRepository globalParametersRepo) {
        this.arrangementItemRepo = arrangementItemRepo;
        this.globalParametersRepo = globalParametersRepo;
    }

    @Override
    public List<ArrangementJob> createArrangementJobs(Arrangement arrangement) {
        List<ArrangementJob> jobList = new ArrayList<>();
        for(ArrangementItem arrangementItem : arrangementItemRepo.findAllByArrangementId(arrangement.getId())){
            ArrangementJob arrangementJob = new ArrangementJob();
            arrangementJob.setId(arrangement.getId());
            //Установим тип запуска для диспетчеризации старта/перезапуска
            arrangementJob.setRunType(getRunType(arrangement));
            arrangementJob.setAccessToolUnit(arrangement.getAccessTool().getName());
            arrangementJob.getErdiJobList().add(new ERDIJob(arrangementItem.getErdi().getId()));
            //Добавляем параметры конкретного ПС/ПАСД
            arrangementJob.getAccessToolParameters().putAll(prepareParameters(arrangement.getAccessTool()));
            //Добавляем глобальные параметры
            arrangementJob.getAccessToolParameters().putAll(prepareGlobalParameters());
            jobList.add(arrangementJob);
        }
        return jobList;
    }

    @Override
    public ArrangementJob createSingleArrangementJob(Arrangement arrangement) {
        ArrangementJob arrangementJob = new ArrangementJob();
        arrangementJob.setId(arrangement.getId());
        //Установим тип запуска для диспетчеризации старта/перезапуска
        arrangementJob.setRunType(getRunType(arrangement));
        arrangementJob.setAccessToolUnit(arrangement.getAccessTool().getName());
        //Добавляем параметры конкретного ПС/ПАСД
        arrangementJob.getAccessToolParameters().putAll(prepareParameters(arrangement.getAccessTool()));
        //Добавляем глобальные параметры
        arrangementJob.getAccessToolParameters().putAll(prepareGlobalParameters());
        return arrangementJob;
    }


    private ArrangementJob.JobRunType getRunType(Arrangement arrangement){
        switch (arrangement.getStatus()){
            case PLANNED:
                return ArrangementJob.JobRunType.START;
            case ACTION_REQUIRED:
                return ArrangementJob.JobRunType.RESTART;
            default:
                throw new AS_15_8_Exception("Error creating arrangement job! Status is not supported: " + arrangement.getStatus());
        }
    }

    private Map<AccessToolParameters, String> prepareParameters(AccessTool accessTool){
        Map<AccessToolParameters, String> result = new HashMap<>();
        if (accessTool.getSearchSystemParameters() != null){
            SearchSystemParameters parameters = accessTool.getSearchSystemParameters();
            result.put(AccessToolParameters.INPUT_DELAY, String.valueOf(parameters.getInputDelay()));
            result.put(AccessToolParameters.SEARCH_SYSTEM_URL, parameters.getSearchSystemUrl());
            result.put(AccessToolParameters.INPUT_SEARCH_FIELD_XPATH_ID, parameters.getInputSearchFieldXpathId());
            result.put(AccessToolParameters.INPUT_SEARCH_FIELD_CSS_SELECTOR, parameters.getInputSearchFieldCssSelector());
            result.put(AccessToolParameters.BUTTON_SEARCH_FIELD_XPATH_ID, parameters.getButtonSearchFieldXpathId());
            result.put(AccessToolParameters.BUTTON_SEARCH_FIELD_CSS_SELECTOR, parameters.getButtonSearchFieldCssSelector());
        } else if (accessTool.getVpnParameters() != null){
            VPN_Parameters parameters = accessTool.getVpnParameters();
            result.put(AccessToolParameters.STUB_URL, parameters.getStubUrl());
            result.put(AccessToolParameters.PROXY_DNS_NAME, parameters.getTechProxyDnsName());
            result.put(AccessToolParameters.PROXY_PORT, parameters.getTechProxyPort());
            result.put(AccessToolParameters.PROXY_USER, parameters.getTechProxyUser());
            result.put(AccessToolParameters.PROXY_PASSWORD, parameters.getTechProxyPassword());
        } else if (accessTool.getProxyParameters() != null) {
            ProxyParameters parameters = accessTool.getProxyParameters();
            result.put(AccessToolParameters.STUB_URL, parameters.getStubUrl());
            result.put(AccessToolParameters.PROXY_DNS_NAME, parameters.getProxyDnsName());
            result.put(AccessToolParameters.PROXY_PORT, parameters.getProxyPort());
            result.put(AccessToolParameters.PROXY_USER, parameters.getProxyUser());
            result.put(AccessToolParameters.PROXY_PASSWORD, parameters.getProxyPassword());
        } else if (accessTool.getAnonymizerParameters() != null) {
            AnonymizerParameters parameters = accessTool.getAnonymizerParameters();
            result.put(AccessToolParameters.STUB_URL, parameters.getStubUrl());
        }
        return result;
    }

    private Map<AccessToolParameters, String> prepareGlobalParameters(){
        return globalParametersRepo.findAll().stream()
            .collect(HashMap::new, (m,v)->m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }
}
