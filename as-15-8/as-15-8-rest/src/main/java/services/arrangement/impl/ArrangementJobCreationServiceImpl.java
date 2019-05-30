package services.arrangement.impl;

import enums.AccessToolParameters;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import model.catalog.*;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.stereotype.Service;
import repositories.ArrangementItemRepository;
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

    public ArrangementJobCreationServiceImpl(ArrangementItemRepository arrangementItemRepo) {
        this.arrangementItemRepo = arrangementItemRepo;
    }

    @Override
    public List<ArrangementJob> createArrangementJobs(Arrangement arrangement) {
        List<ArrangementJob> jobList = new ArrayList<>();
        for(ArrangementItem arrangementItem : arrangementItemRepo.findAllByArrangementId(arrangement.getId())){
            ArrangementJob arrangementJob = new ArrangementJob();
            arrangementJob.setId(arrangement.getId());
            arrangementJob.setAccessToolUnit(arrangement.getAccessTool().getName());
            arrangementJob.getErdiJobList().add(new ERDIJob(arrangementItem.getErdi().getId()));
            arrangementJob.getAccessToolParameters().putAll(prepareParameters(arrangement.getAccessTool()));
            jobList.add(arrangementJob);
        }
        return jobList;
    }

    private Map<AccessToolParameters, String> prepareParameters(AccessTool accessTool){
        Map<AccessToolParameters, String> result = new HashMap<>();
        if (accessTool.getSearchSystemParameters() != null){
            SearchSystemParameters parameters = accessTool.getSearchSystemParameters();
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
}
