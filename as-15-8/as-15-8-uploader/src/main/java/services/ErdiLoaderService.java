package services;

import exceptions.ExceptionErdiLoad;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.converters.CheckUnitTypeValueConverter;
import model.enums.BlockType;
import model.enums.ParamSor;
import model.enums.UrgencyType;
import model.rest.*;
import model.scheme.*;
import model.scheme.Decision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import repositories.*;
import repositories.impl.ParameterRepositoryImpl;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ErdiLoaderService {

    private final ContentRepository contentRepository;
    private final AddonRepository addonRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final AddonVersionRepository addonVersionRepository;
    private final DecisionRepository decisionRepository;
    private final EntityManager em;
    private final JdbcTemplate jdbcTemplate;
    private final RegisterRepository registerRepository;
    private final ContentInfoRepository contentInfoRepository;
    private final ContentHistoryRepository contentHistoryRepository;
    private final ContentResourcesRepository contentResourcesRepository;
    private final ContentDelRepository contentDelRepository;
    private final ParameterRepositoryImpl parameterRepository;


    @Transactional
    public void addAllContents(boolean isDelta, RegisterRest registerRest, List<ContentRest> contentRests){
        System.out.println("----- addAllContents: " + isDelta + ", size = " + contentRests.size());

        ContentVersion newContentVersion = createContentVersion(registerRest, isDelta);
        ContentVersion contentVersion = contentVersionRepository.save(newContentVersion);
        System.out.println(registerRest);
        System.out.println(contentVersion);

        AddonVersion addonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();
        if (addonVersion == null){
            addonVersion = new AddonVersion();
            addonVersion.setPpnDate(new Date());
            addonVersionRepository.save(addonVersion);
        }

        System.out.println("Content version: " + newContentVersion.getId());

        Register register = createRegister(registerRest);
        registerRepository.save(register);

        List<List<ContentRest>> parts = new ArrayList<>();
        for (int i = 0; i < contentRests.size(); i++) {
            if (i%10000 == 0){
                parts.add(new ArrayList<>());
            }
            List<ContentRest> list = parts.get(parts.size()-1);
            list.add(contentRests.get(i));
        }

        int count = 0;
        for(List<ContentRest> listContentRest : parts){
            count += listContentRest.size();
            System.out.println("---> addAllContents : " + count);

            List<ContentFull> newFullContents = new ArrayList<>();
            Map<ContentFull, Content> mapChangeContents = new LinkedHashMap<>();
            Map<ContentDelete, Content> mapDeleteContents = new LinkedHashMap<>();

            filterContents(listContentRest, newFullContents, mapChangeContents, mapDeleteContents);

            System.out.println("----- newFullContents = " + newFullContents.size());
            //System.out.println(newFullContents);
            System.out.println("----- mapChangeContents = " + mapChangeContents.size());
            //System.out.println(mapChangeContents);
            System.out.println("----- mapDeleteContents = " + mapDeleteContents.size());
            //System.out.println(mapDeleteContents);

            addContents(newFullContents, mapChangeContents, contentVersion);
            changeContents(mapChangeContents, contentVersion, null);
            deleteContents(mapDeleteContents, contentVersion, registerRest);
        }
    }


    @Transactional
    public void startAddContent(boolean isDelta, RegisterRest registerRest) throws ExceptionErdiLoad
    {
        checkProcessOperation();

        ContentVersion contentVersion = contentVersionRepository.save(createContentVersion(registerRest, isDelta));

        AddonVersion addonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();
        if (addonVersion == null){
            addonVersion = new AddonVersion();
            addonVersion.setPpnDate(new Date());
            addonVersionRepository.save(addonVersion);
        }

        //addRegister(registerRest);

        parameterRepository.setParameterValue(ParamSor.PROCESS_CONTENT_VERSION.name(), ""+contentVersion.getId());
    }

    /*
    @Transactional
    public void processAddContent(List<ContentRest> fullContents, List<ContentDelete> forDeleteContents) throws ExceptionErdiLoad
    {
        ContentVersion contentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        AddonVersion addonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();

        List<ContentFull> filteredNewFullContents = new ArrayList<>();
        List<ContentFull> filteredModFullContents = new ArrayList<>();
        List<Content> filteredModContents = new ArrayList<>();
        List<Content> filteredDeleteContents = new ArrayList<>();

        filterContents(fullContents, filteredNewFullContents, filteredModFullContents, filteredModContents, filteredDeleteContents);

        List<Long> ids = forDeleteContents.stream()
                .map(contentDelete -> contentDelete.id)
                .collect(Collectors.toList());
        if (ids.size() > 0){
            filteredDeleteContents = contentRepository.findByErdiIdIn(ids);
        }

        addContents(filteredNewFullContents, contentVersion, addonVersion);

        changeContents(filteredModFullContents, filteredModContents, contentVersion, addonVersion);

        deleteContents(filteredDeleteContents);
    }
    */

    @Transactional
    public void finishAddContentVersion() throws ExceptionErdiLoad
    {
        parameterRepository.setParameterValue(ParamSor.PROCESS_CONTENT_VERSION.name(), null);
    }


    public void checkProcessOperation() throws ExceptionErdiLoad {
        String processContentVersion = parameterRepository.getParameterValue(ParamSor.PROCESS_CONTENT_VERSION.name());
        String processAddonVersion = parameterRepository.getParameterValue(ParamSor.PROCESS_ADDON_VERSION.name());

        String processRemoveContentVersion = parameterRepository.getParameterValue(ParamSor.PROCESS_REMOVE_CONTENT_VERSION.name());
        String processRemoveAddonVersion = parameterRepository.getParameterValue(ParamSor.PROCESS_REMOVE_ADDON_VERSION.name());

        if (processContentVersion != null){
            throw new ExceptionErdiLoad("Загрузка конетнта не завершилась! Версия " + processContentVersion);
        }
        if (processAddonVersion != null){
            throw new ExceptionErdiLoad("Загрузка аддона не завершилась! Версия " + processAddonVersion);
        }
        if (processRemoveContentVersion != null){
            throw new ExceptionErdiLoad("Удаление конетнта не завершилась! Версия >=" + processRemoveContentVersion);
        }
        if (processRemoveAddonVersion != null){
            throw new ExceptionErdiLoad("Удаление аддона не завершилась! Версия =>" + processRemoveAddonVersion);
        }
    }

    @Transactional
    public void removeLastContentVersion(){
        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        if (lastContentVersion == null)
            return;

        System.out.println("Remove version: " + lastContentVersion.getId());

        String sql =
                "update sor.content_history h set end_dt=to_date('30000101', 'YYYYMMDD')\n" +
                "where end_dt in (select ch.st_dt from sor.content_history ch where ch.content_version_id=?)";

        jdbcTemplate.update(sql, lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_history where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_info where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_resources where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.decision where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_del where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content where init_content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_version where id = ?", lastContentVersion.getId());
    }

    public void removeAddonsByVersion(Long addonId){
    }


    public Register createRegister(RegisterRest registerRest){
        if (registerRest != null) {
            Register register = new Register();
            register.setUpdateTime(registerRest.updateTime);
            register.setUpdateTimeUrgently(registerRest.updateTimeUrgently);
            register.setFormatVersion(registerRest.formatVersion);
            return register;
        }
        return null;
    }

    public void filterContents(List<ContentRest> contents,
                               List<ContentFull> newFullContents,
                               Map<ContentFull, Content> mapChangeContents,
                               Map<ContentDelete, Content> mapDeleteContents)
    {
        newFullContents.clear();
        mapChangeContents.clear();
        mapDeleteContents.clear();

        List<String> ids_full = contents.stream()
                .filter(content -> content instanceof ContentFull)
                .map(content -> ""+content.id)
                .collect(Collectors.toList());

        if (ids_full.size() > 0) {
            List<Content> list = contentRepository.findByErdiIdIn(ids_full);
            Map<String, Content> mapContents = new HashMap<>();

            for (Content cnt : list)
                mapContents.put(cnt.getErdiId(), cnt);

            for (ContentRest contentRest : contents) {
                Content content = mapContents.get(""+contentRest.id);
                if (contentRest instanceof ContentFull) {
                    if (content == null){
                        newFullContents.add((ContentFull) contentRest);
                    }
                    else {
                        mapChangeContents.put((ContentFull) contentRest, content);
                    }
                }
            }
        }

        List<String> ids_del = contents.stream()
                .filter(content -> content instanceof ContentDelete)
                .map(content -> ""+content.id)
                .collect(Collectors.toList());

        if (ids_del.size() > 0) {
            List<Content> list = contentRepository.findByErdiIdIn(ids_del);
            Map<String, Content> mapContents = new HashMap<>();

            for (Content cnt : list)
                mapContents.put(cnt.getErdiId(), cnt);

            for (ContentRest contentRest : contents) {
                Content content = mapContents.get(""+contentRest.id);
                if (contentRest instanceof ContentDelete && content != null) {
                    mapDeleteContents.put((ContentDelete) contentRest, content);
                }
            }
        }
    }

    public void addContents(List<ContentFull> fullContents, Map<ContentFull, Content> mapNewContents, ContentVersion contentVersion){
        if (fullContents.size() == 0)
            return;

        List<Content> newContents = new ArrayList<>();
        Map<String, ContentFull> mapFullContent = new HashMap<>();
        for (ContentFull fc : fullContents){
            Content cnt = new Content();
            cnt.setErdiId("" + fc.id);
            cnt.setContentVersion(contentVersion);
            newContents.add(cnt);
            mapFullContent.put(""+fc.id, fc);
        }
        log.info("Start add content... size = " + fullContents.size());
        List<Content> savedContents = contentRepository.saveAll(newContents);
        log.info("End");

        for(Content content : savedContents){
            ContentFull contentFull = mapFullContent.get(content.getErdiId());
            if (contentFull != null){
                mapNewContents.put(contentFull, content);
            }
        }

        System.out.println("************************ ADD");
        System.out.println("newDecisionList: " + savedContents.size());
        //System.out.println(savedContents);
    }


    public void changeContents(Map<ContentFull, Content> mapChangeContents, ContentVersion contentVersion, AddonVersion addonVersion){
        if (mapChangeContents.size() == 0)
            return;

        List<ContentInfo> newContentsInfoList = new ArrayList<>();
        List<ContentResources> newContentResourcesList = new ArrayList<>();
        List<ContentHistory> newContentHistoryList = new ArrayList<>();

        for (ContentFull cntFull : mapChangeContents.keySet()){
            Content cnt = mapChangeContents.get(cntFull);

            newContentsInfoList.add(createContentInfo(cnt, contentVersion, cntFull));
            newContentResourcesList.addAll(createContentResources(cnt, contentVersion, cntFull));
            newContentHistoryList.add(createContentHistory(cnt, contentVersion, addonVersion, cntFull.includeTime, null));
        }

        System.out.println("************************ CHANGE");
        System.out.println("newContentsInfoList: " + newContentsInfoList.size());
        //System.out.println(newContentsInfoList);
        System.out.println("newContentHistoryList: " + newContentHistoryList.size());
        //System.out.println(newContentHistoryList);
        System.out.println("newContentResourcesList: " + newContentResourcesList.size());
        //System.out.println(newContentResourcesList);

        log.info("Start save ifo content... size = " + newContentsInfoList.size());
        contentInfoRepository.saveAll(newContentsInfoList);

        log.info("Start save history... size = " + newContentHistoryList.size());
        contentHistoryRepository.saveAll(newContentHistoryList);

        log.info("Start save resources... size = " + newContentResourcesList.size());
        contentResourcesRepository.saveAll(newContentResourcesList);
    }

    public void deleteContents(Map<ContentDelete, Content> mapDeleteContents, ContentVersion contentVersion, RegisterRest registerRest){

        List<ContentHistory> contentHistoryList = new ArrayList<>();
        //List<ContentDel> contentDelList = new ArrayList<>();

        for(ContentDelete contentDelete : mapDeleteContents.keySet()){
            Content content = mapDeleteContents.get(contentDelete);
            ContentHistory newContentHistory =
                    createContentHistory(content, contentVersion, null, registerRest.updateTime, registerRest.updateTime);
            contentHistoryList.add(newContentHistory);
            //contentDelList.add(createContentDel(content, contentVersion));
        }
        System.out.println("************************ DELETE");
        System.out.println("contentHistoryList: " + contentHistoryList.size());
        //System.out.println(contentHistoryList);
        //System.out.println("contentDelList: " + contentDelList.size());
        //System.out.println(contentDelList);

        //contentDelRepository.saveAll(contentDelList);

        log.info("Start save delete history... size = " + contentHistoryList.size());
        contentHistoryRepository.saveAll(contentHistoryList);
        log.info("End");
    }

    private ContentVersion createContentVersion(RegisterRest registerRest, boolean isDelta){
        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        Date regUpdateTime = isDelta ? (lastContentVersion != null ? lastContentVersion.getRegUpdateTime() : registerRest.updateTime) : registerRest.updateTime;
        Date deltaUpdateTime = isDelta ? registerRest.updateTime : null;

        ContentVersion contentVersion = new ContentVersion();
        contentVersion.setRegUpdateTime(regUpdateTime);
        contentVersion.setDeltaUpdateTime(deltaUpdateTime);
        contentVersion.setPpnDate(new Date());          // проставляется автоматом

        return contentVersion;
    }


    private Decision createDecision(Content content, ContentVersion contentVersion, ContentFull contentFull){
        Decision decision = new Decision();
        decision.setDate(contentFull.decision.date);
        decision.setNumber(contentFull.decision.number);
        decision.setOrg(contentFull.decision.org);
        decision.setContent(content);
        decision.setContentVersion(contentVersion);
        return decision;
    }

    private ContentInfo createContentInfo(Content content, ContentVersion contentVersion, ContentFull contentFull){
        ContentInfo cntInfo = new ContentInfo();
        cntInfo.setContent(content);
        cntInfo.setHash(contentFull.hash);
        cntInfo.setTs(contentFull.ts);
        cntInfo.setEntryTypeId(contentFull.entryTypeId);
        cntInfo.setContentVersion(contentVersion);
        cntInfo.setBlockType(BlockType.parse(contentFull.blockType, null));
        cntInfo.setUrgencyType(UrgencyType.parse(contentFull.urgencyType, null));
        cntInfo.setIncludeTime(contentFull.includeTime);
        return cntInfo;
    }

    private List<ContentResources> createContentResources(Content content, ContentVersion contentVersion, ContentFull contentFull){
        List<ContentResources> list = new ArrayList<>();
        if (contentFull.types != null){
            for(ResourceType resourceType : contentFull.types){
                ContentResources contentResources = new ContentResources();
                contentResources.setContent(content);
                contentResources.setValue(resourceType.value);
                contentResources.setTs(contentFull.ts);
                contentResources.setCheckUnitType(CheckUnitTypeValueConverter.convertToType(resourceType));
                contentResources.setContentVersion(contentVersion);
                list.add(contentResources);
            }
        }
        return list;
    }

    private ContentHistory createContentHistory(
            Content content,
            ContentVersion contentVersion,
            AddonVersion addonVersion,
            Date startDate,
            Date endDate){
        ContentHistory contentHistory = new ContentHistory();
        contentHistory.setContent(content);
        contentHistory.setPpnDate(new Date());
        contentHistory.setContentVersion(contentVersion);   // если NULL, то выставляется триггером
        contentHistory.setAddonVersion(addonVersion);       // если NULL, то выставляется триггером
        contentHistory.setStartDate(startDate);
        contentHistory.setEndDate(endDate);                 // если NULL, то выставляется триггером
        return contentHistory;
    }

    private ContentDel createContentDel(
            Content content,
            ContentVersion contentVersion){
        ContentDel contentDel = new ContentDel();
        contentDel.setContent(content);
        contentDel.setContentVersion(contentVersion);
        return contentDel;
    }

    @Transactional
    public void clearAllScheme(){
        System.out.println("----------- start contentRepository");
        //contentRepository.deleteAll();

        /*
        Query q1 = em.createQuery("truncate table ContentResources");
        Query q2 = em.createQuery("DELETE FROM ContentHistory");
        Query q3 = em.createQuery("DELETE FROM ContentInfo");
        Query q4 = em.createQuery("DELETE FROM Decision");
        Query q5 = em.createQuery("DELETE FROM ContentDel");
        Query q6 = em.createQuery("DELETE FROM Addon");
        Query q7 = em.createQuery("DELETE FROM Content");
        Query q8 = em.createQuery("DELETE FROM ContentVersion");
        Query q9 = em.createQuery("DELETE FROM AddonVersion");

        q1.executeUpdate();
        q2.executeUpdate();
        q3.executeUpdate();
        q4.executeUpdate();
        q5.executeUpdate();
        q6.executeUpdate();
        q7.executeUpdate();
        q8.executeUpdate();
        q9.executeUpdate();
        */
    }
}
