package services;

import exceptions.ExceptionErdiLoad;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.converters.CheckUnitTypeValueConverter;
import model.enums.BlockType;
import model.enums.UrgencyType;
import model.response.DeltaIdEntry;
import model.rest.*;
import model.scheme.Decision;
import model.scheme.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import repositories.*;
import repositories.impl.ParameterRepositoryExtend;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private final ParameterRepositoryExtend parameterRepository;

    // todo- удалить
    private static List<Date> timeLabels = new ArrayList<>();


    @Transactional
    public boolean fillContents(DeltaIdEntry deltaIdEntry, RegisterRest registerRest, List<ContentRest> contentRests) throws ExceptionErdiLoad {
        log.info("===== START FILL ERDI =====");
        log.info("Content size = {}", contentRests.size());
        log.info("Delta = {}", contentRests.size(), (deltaIdEntry == null ? "<Full ERDI>" : deltaIdEntry.toString()));

        try {
            if (deltaIdEntry != null){
                ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
                if (fullContentVersion == null){
                    throw new ExceptionErdiLoad("Попытка загрузить дельту ЕРДИ когда отсутствует полное ЕРДИ");
                }
            }

            // пропускаем загрузку, контента нет
            if (contentRests.size() == 0) {
                return true;
            }

            ContentVersion newContentVersion = createContentVersion(registerRest, deltaIdEntry);
            ContentVersion contentVersion = contentVersionRepository.save(newContentVersion);

            AddonVersion addonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();
            if (addonVersion == null){
                addonVersion = new AddonVersion();
                addonVersion.setPpnDate(new Date());
                addonVersionRepository.save(addonVersion);
            }

            log.info("ContentVersion = {}", contentVersion);
            log.info("AddonVersion = {}", addonVersion);

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
                log.info("=== Partition fill count: " + count);

                List<ContentFull> newFullContents = new ArrayList<>();
                Map<ContentFull, Content> mapChangeContents = new LinkedHashMap<>();
                Map<ContentDelete, Content> mapDeleteContents = new LinkedHashMap<>();

//                timeLabels.clear();
//                timeLabels.add(new Date());
                filterContents(listContentRest, newFullContents, mapChangeContents, mapDeleteContents);
//                timeLabels.add(new Date());

                addContents(newFullContents, mapChangeContents, contentVersion);
//                timeLabels.add(new Date());
                changeContents(mapChangeContents, contentVersion, null);
//                timeLabels.add(new Date());
                deleteContents(mapDeleteContents, contentVersion, registerRest);
//                timeLabels.add(new Date());

//                List<String> res = new ArrayList<>();
//                for (int i=1; i<timeLabels.size(); i++)
//                    res.add(i+": " + (timeLabels.get(i).getTime() - timeLabels.get(i-1).getTime()) + " ms");
//                log.info(res.toString());
            }
        }
        catch (Exception e){
            throw new ExceptionErdiLoad(e);
        }

        log.info("===== FINISH FILL ERDI =====");
        return true;
    }

    @Transactional
    public void removeLastContentVersion(){
        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        if (lastContentVersion == null)
            return;

        log.info("Remove version: " + lastContentVersion.getId());

        String sql =
                "update sor.content_history h set end_dt=to_date('30000101', 'YYYYMMDD')\n" +
                "where end_dt in (select ch.st_dt from sor.content_history ch where h.content_id = ch.content_id and ch.content_version_id=?)";

        jdbcTemplate.update(sql, lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_history where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_info where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_resources where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.decision where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_del where content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content where init_content_version_id = ?", lastContentVersion.getId());
        jdbcTemplate.update("delete FROM sor.content_version where id = ?", lastContentVersion.getId());

        log.info("Removing version completed");
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

        List<Long> ids_full = contents.stream()
                .filter(content -> content instanceof ContentFull)
                .map(content -> content.id)
                .collect(Collectors.toList());

        if (ids_full.size() > 0) {
            List<Content> list = contentRepository.findByErdiIdIn(ids_full);
            Map<Long, Content> mapContents = new HashMap<>();

            for (Content cnt : list)
                mapContents.put(cnt.getErdiId(), cnt);

            for (ContentRest contentRest : contents) {
                Content content = mapContents.get(contentRest.id);
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

        List<Long> ids_del = contents.stream()
                .filter(content -> content instanceof ContentDelete)
                .map(content -> content.id)
                .collect(Collectors.toList());

        if (ids_del.size() > 0) {
            List<Content> list = contentRepository.findByErdiIdIn(ids_del);
            Map<Long, Content> mapContents = new HashMap<>();

            for (Content cnt : list)
                mapContents.put(cnt.getErdiId(), cnt);

            for (ContentRest contentRest : contents) {
                Content content = mapContents.get(contentRest.id);
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
        List<Decision> newDecision = new ArrayList<>();

        Map<Long, ContentFull> mapFullContent = new HashMap<>();
        for (ContentFull fc : fullContents){
            Content cnt = new Content();
            cnt.setErdiId(fc.id);
            cnt.setContentVersion(contentVersion);
            newContents.add(cnt);
            mapFullContent.put(fc.id, fc);
        }
        //log.info("Start add content... size = " + fullContents.size());
        List<Content> savedContents = contentRepository.saveAll(newContents);
        //log.info("End");

        for(Content content : savedContents){
            ContentFull contentFull = mapFullContent.get(content.getErdiId());
            if (contentFull != null){
                mapNewContents.put(contentFull, content);
                newDecision.add(createDecision(content, contentVersion, contentFull));
            }
        }

        decisionRepository.saveAll(newDecision);

        //System.out.println("************************ ADD");
        //System.out.println("newDecisionList: " + savedContents.size());
        //System.out.println(savedContents);
    }


    public void changeContents(Map<ContentFull, Content> mapChangeContents, ContentVersion contentVersion, AddonVersion addonVersion){
        if (mapChangeContents.size() == 0)
            return;

        List<ContentInfo> newContentsInfoList = new ArrayList<>();
        List<ContentResource> newContentResourceList = new ArrayList<>();
        List<ContentHistory> newContentHistoryList = new ArrayList<>();

        for (ContentFull cntFull : mapChangeContents.keySet()){
            Content cnt = mapChangeContents.get(cntFull);

            newContentsInfoList.add(createContentInfo(cnt, contentVersion, cntFull));
            newContentResourceList.addAll(createContentResources(cnt, contentVersion, cntFull));
            newContentHistoryList.add(createContentHistory(cnt, contentVersion, addonVersion, cntFull.includeTime, null));
        }

        //System.out.println("************************ CHANGE");
        //System.out.println("newContentsInfoList: " + newContentsInfoList.size());
        //System.out.println(newContentsInfoList);
        //System.out.println("newContentHistoryList: " + newContentHistoryList.size());
        //System.out.println(newContentHistoryList);
        //System.out.println("newContentResourcesList: " + newContentResourcesList.size());
        //System.out.println(newContentResourcesList);

        //log.info("Start save ifo content... size = " + newContentsInfoList.size());
        contentInfoRepository.saveAll(newContentsInfoList);

        //log.info("Start save history... size = " + newContentHistoryList.size());
        contentHistoryRepository.saveAll(newContentHistoryList);

        //log.info("Start save resources... size = " + newContentResourcesList.size());
        contentResourcesRepository.saveAll(newContentResourceList);
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
        //System.out.println("************************ DELETE");
        //System.out.println("contentHistoryList: " + contentHistoryList.size());
        //System.out.println(contentHistoryList);
        //System.out.println("contentDelList: " + contentDelList.size());
        //System.out.println(contentDelList);

        //contentDelRepository.saveAll(contentDelList);

        //log.info("Start save delete history... size = " + contentHistoryList.size());
        contentHistoryRepository.saveAll(contentHistoryList);
        //log.info("End");
    }

    private ContentVersion createContentVersion(RegisterRest registerRest, DeltaIdEntry deltaIdEntry) throws ParseException {
        ContentVersion contentVersion = new ContentVersion();
        contentVersion.setRegUpdateTime(deltaIdEntry == null ? registerRest.updateTime : null);
        contentVersion.setDeltaUpdateTime(deltaIdEntry == null ? null : deltaIdEntry.actualDate);
        contentVersion.setPpnDate(new Date());          // проставляется автоматом
        contentVersion.setDeltaId(deltaIdEntry == null ? null : deltaIdEntry.deltaId);

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
        cntInfo.setEntryTypeId(contentFull.entryTypeId.toString());
        cntInfo.setContentVersion(contentVersion);
        cntInfo.setBlockType(BlockType.parse(contentFull.blockType, null));
        cntInfo.setUrgencyType(UrgencyType.parse(contentFull.urgencyType, null));
        cntInfo.setIncludeTime(contentFull.includeTime);
        return cntInfo;
    }

    private List<ContentResource> createContentResources(Content content, ContentVersion contentVersion, ContentFull contentFull){
        List<ContentResource> list = new ArrayList<>();
        if (contentFull.types != null){
            for(ResourceType resourceType : contentFull.types){
                ContentResource contentResource = new ContentResource();
                contentResource.setContent(content);
                contentResource.setValue(resourceType.value);
                contentResource.setTs(contentFull.ts);
                contentResource.setCheckUnitType(CheckUnitTypeValueConverter.convertToType(resourceType));
                contentResource.setContentVersion(contentVersion);
                list.add(contentResource);
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
