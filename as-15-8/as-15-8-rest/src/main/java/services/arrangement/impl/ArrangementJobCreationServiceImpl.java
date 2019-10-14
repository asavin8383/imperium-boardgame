package services.arrangement.impl;

import enums.AccessToolParameter;
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

import java.util.HashMap;
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
    public ArrangementJob createArrangementJob(Arrangement arrangement) {
        ArrangementJob arrangementJob = createBriefArrangementJob(arrangement);
        //Установим тип запуска для диспетчеризации старта/перезапуска
        arrangementJob.setRunType(getRunType(arrangement));
        arrangementJob.setAccessToolUnit(arrangement.getAccessTool().getName());
        //Добавляем параметры конкретного ПС/ПАСД
        arrangementJob.getAccessToolParameters().putAll(prepareParameters(arrangement.getAccessTool()));
        //Добавляем глобальные параметры
        arrangementJob.getAccessToolParameters().putAll(prepareGlobalParameters());
        for(ArrangementItem arrangementItem : arrangementItemRepo.findAllByArrangementId(arrangement.getId())){
            arrangementJob.getErdiJobList().add(new ERDIJob(arrangementItem.getErdi().getId()));
        }
        return arrangementJob;
    }

    @Override
    public ArrangementJob createBriefArrangementJob(Arrangement arrangement){
        ArrangementJob arrangementJob = new ArrangementJob();
        arrangementJob.setId(arrangement.getId());
        return arrangementJob;
    }

    private ArrangementJob.JobRunType getRunType(Arrangement arrangement){
        switch (arrangement.getStatus()){
            case NEW:
                return ArrangementJob.JobRunType.START;
            case ACTION_REQUIRED:
                return ArrangementJob.JobRunType.RESTART;
            default:
                throw new AS_15_8_Exception("Error creating arrangement job! Status is not supported: " + arrangement.getStatus());
        }
    }

    private Map<AccessToolParameter, String> prepareParameters(AccessTool accessTool){
        Map<AccessToolParameter, String> result = new HashMap<>();
        if (accessTool.getSearchSystemParameters() != null){
            SearchSystemParameters parameters = accessTool.getSearchSystemParameters();
            result.put(AccessToolParameter.INPUT_DELAY, String.valueOf(parameters.getInputDelay()));
            result.put(AccessToolParameter.SEARCH_SYSTEM_URL, parameters.getSearchSystemUrl());
            result.put(AccessToolParameter.SEARCH_SYSTEM_RESULT_PAGE_TYPE, parameters.getResultPageType());
            result.put(AccessToolParameter.SEARCH_SYSTEM_XPATH_INPUT_FIELD, parameters.getInputFieldXpath());
            result.put(AccessToolParameter.SEARCH_SYSTEM_XPATH_CAPTCHA, parameters.getCaptchaXpath());
            result.put(AccessToolParameter.SEARCH_SYSTEM_XPATH_ITEM_LINK, parameters.getItemLinkXpath());
            result.put(AccessToolParameter.SEARCH_SYSTEM_XPATH_NEXT_PAGE, parameters.getNextPageXpath());
        } else if (accessTool.getVpnParameters() != null){
            VPN_Parameters parameters = accessTool.getVpnParameters();
            result.put(AccessToolParameter.STUB_URL, parameters.getStubUrl());
            result.put(AccessToolParameter.PROXY_DNS_NAME, parameters.getTechProxyDnsName());
            result.put(AccessToolParameter.PROXY_PORT, parameters.getTechProxyPort());
            result.put(AccessToolParameter.PROXY_USER, parameters.getTechProxyUser());
            result.put(AccessToolParameter.PROXY_PASSWORD, parameters.getTechProxyPassword());
        } else if (accessTool.getProxyParameters() != null) {
            ProxyParameters parameters = accessTool.getProxyParameters();
            result.put(AccessToolParameter.STUB_URL, parameters.getStubUrl());
            result.put(AccessToolParameter.PROXY_DNS_NAME, parameters.getProxyDnsName());
            result.put(AccessToolParameter.PROXY_PORT, parameters.getProxyPort());
            result.put(AccessToolParameter.PROXY_USER, parameters.getProxyUser());
            result.put(AccessToolParameter.PROXY_PASSWORD, parameters.getProxyPassword());
        } else if (accessTool.getAnonymizerParameters() != null) {
            AnonymizerParameters parameters = accessTool.getAnonymizerParameters();
            result.put(AccessToolParameter.STUB_URL, parameters.getStubUrl());
            result.put(AccessToolParameter.PROXY_DNS_NAME, parameters.getProxyDnsName());
            result.put(AccessToolParameter.PROXY_PORT, parameters.getProxyPort());
        }
        return result;
    }

    private Map<AccessToolParameter, String> prepareGlobalParameters(){
        return globalParametersRepo.findAll().stream()
            .collect(HashMap::new, (m,v)->m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }
}
