package services;

import checkUnits.CheckUnitType;
import exceptions.AS_15_8_POD_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.actualViews.ContentCheckUnit;
import model.projection.ContentView;
import model.scheme.DomainMask;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import repositories.ContentCheckUnitRepository;
import repositories.ContentViewRepository;
import repositories.DomainMaskRepo;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames={"sor_content"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ContentService {

    private final ContentViewRepository viewRepository;
    private final ContentCheckUnitRepository contentCheckUnitRepository;
    private final DomainMaskRepo domainMaskRepo;
    private final EntityManagerFactory emf;
    private EntityManager em;

    @Cacheable
    public Optional<ContentView> getFormalErdiView(Long id) {
        return viewRepository.findById(id);
    }

    /**
     * Получение списка актуальных чек-юнитов по ИД ЕРДИ
     * @param erdiId ИД ЕРДИ
     * @return список актуальных чек-юнитов
     */
    public List<ContentCheckUnit> getActualCheckUnits(Long erdiId){
        return contentCheckUnitRepository.findAllByErdId(erdiId);
    }

    public List<ContentCheckUnit> getActualCheckUnits(List<Long> erdiIds){
        return contentCheckUnitRepository.findAllByErdIds(erdiIds);
    }

    public Long getCheckUnitsCount(List<Long> erdiIds) {
        Long erdiCountDomainMaskExclude = contentCheckUnitRepository.findAllByErdIdsExclude(erdiIds, CheckUnitType.DOMAIN_MASK);
        Long erdiDomainMaskCount = getCheckUnitsInDomainMaskRepo(erdiIds);
        return erdiCountDomainMaskExclude + erdiDomainMaskCount;
    }

    private Long getCheckUnitsInDomainMaskRepo(List<Long> erdiIds) {
        AtomicReference<Long> erdiDomainMaskCount = new AtomicReference<>(0L);
        List<String> erdiDomainMasksCheckUnitValues = contentCheckUnitRepository.findAllCheckUnitsValueBy(erdiIds, CheckUnitType.DOMAIN_MASK);
        erdiDomainMasksCheckUnitValues.forEach(checkUnitValue-> {
            Set<DomainMask> domainMasks = domainMaskRepo.findAllDomainMasksLike(checkUnitValue);
            domainMasks.forEach(domainMask -> {
                long domainsCount = domainMask.getDomains().size();
                erdiDomainMaskCount.set(erdiDomainMaskCount.get() + domainsCount);
            });
        });
        return erdiDomainMaskCount.get();
    }

    public ResponseEntity filterContentView(Pageable pageable, List<Long> contentIds) {

        Sort.Order order = pageable.getSort().stream().findFirst().orElseThrow(() ->
                new AS_15_8_POD_Exception("фильтрация записей ерди невозможна, сортировка не задана!"));

        Page<ContentView> pageContent;
        em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            createTempContentTable();
            fillTempContentTable(contentIds);
            List<ContentView> contentViewsSorted = filterContentView(order.getDirection().toString(), order.getProperty());
            pageContent = createPageable(pageable, contentViewsSorted, contentIds.size());
            return ResponseEntity.ok().body(pageContent);
        } catch (Exception ex) {
            if(transaction.isActive())
                transaction.rollback();

            log.error("Ошибка при получении списка ЕРДИ", ex);

            List<String> errorMessages = getErrorMessagesByCause(ex);

            return ResponseEntity.badRequest().body(String.join(":\n", errorMessages));
        } finally {
            dropTempTable();
            transaction.commit();

            em.close();
        }
    }

    private void logTime(String msg, LocalDateTime startTime) {
        long timeDuration = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
        log.info(msg + " {} секунд", timeDuration);
    }

    private List<String> getErrorMessagesByCause(Exception ex) {
        Throwable throwable = ex;
        List<String> resultErrorMessage = new ArrayList<>();
        while (throwable != null) {
            resultErrorMessage.add(throwable.getMessage());
            throwable = throwable.getCause();
        }
        return resultErrorMessage;
    }

    private void createTempContentTable() {
        LocalDateTime startTime = LocalDateTime.now();

//        String queryStr = "CREATE TEMPORARY TABLE content_temp ("
//                + "contentId bigint NOT NULL PRIMARY KEY)";

        String queryStr = "CREATE TEMPORARY TABLE content_temp ("
                + "contentId bigint)";

        em.createNativeQuery(queryStr).executeUpdate();

        logTime("Create temp таблицы ", startTime);
    }

    private void fillTempContentTable(List<Long> contentIds) {
        LocalDateTime startTime = LocalDateTime.now();
        em.createNativeQuery("insert into content_temp (contentId) values(unnest(array" + contentIds.toString() + "))").executeUpdate();
        logTime("Insert в temp таблицу ", startTime);
    }

    private void unnestTestLog(List<Long> contentIds) {
        LocalDateTime startTime = LocalDateTime.now();

        List<String> res = em.createNativeQuery("explain analyze select unnest(array" + contentIds.toString() + ")")
                .getResultList();

        String result = res.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining("\n"));

        logTime("Unnest в temp таблицу ", startTime);
        log.info("Unnest analyze log");
        log.info(result);
    }

    private List<ContentView> filterContentView(String sortingDirection, String sortingColumn) {
        LocalDateTime startTime = LocalDateTime.now();

        List<ContentView> contentViews = em.createNativeQuery("select c.* from sor.content_view c " +
                        "join content_temp t " +
                        "on t.contentId = c.id "// +
//                        "ORDER BY " + sortingColumn + " " + sortingDirection
                , ContentView.class).getResultList();

        logTime("Join c temp таблицей ", startTime);
        return contentViews;
    }

    private Page<ContentView> createPageable(Pageable pageable, List<ContentView> result, Integer totalElements) {
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > result.size() ? result.size() : (start + pageable.getPageSize());

        return new PageImpl<ContentView>((result.subList(start, end)), pageable, totalElements);
    }

    private void dropTempTable() {
        em.createNativeQuery("DROP TABLE content_temp").executeUpdate();
    }

    public String convertCamelCaseToSnakeCase(String parse) {
        parse = parse.replaceAll("([^_A-Z])([A-Z])", "$1_$2");
        parse.toLowerCase();
        return parse;

    }
}
