package services;

import checkUnits.CheckUnitType;
import com.google.common.collect.Lists;
import exceptions.AS_15_8_POD_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.actualViews.ContentCheckUnit;
import model.projection.ContentView;
import model.scheme.DomainMask;

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
    private final int trafficContentViewBatchSize = 10000;

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
            List<ContentView> contentViewsSorted = filterContentViewUnnest(order.getDirection().toString(),
                    order.getProperty(),
                    contentIds);
            pageContent = createPageable(pageable, contentViewsSorted, contentIds.size());
            return ResponseEntity.ok().body(pageContent);
        } catch (Exception ex) {
            if(transaction.isActive())
                transaction.rollback();

            log.error("Ошибка при получении списка ЕРДИ", ex);
            List<String> errorMessages = getErrorMessagesByCause(ex);
            return ResponseEntity.badRequest().body(String.join(":\n", errorMessages));
        } finally {
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

    private List<ContentView> filterContentViewUnnest(String sortingDirection, String sortingColumn, List<Long> contentIds) {
        LocalDateTime startTime = LocalDateTime.now();
        List<ContentView> result = new ArrayList<>();
        List<List<Long>> subContentIds = Lists.partition(contentIds, trafficContentViewBatchSize);
        subContentIds.forEach(subContent -> {
            List<ContentView> contentViewsBstch = em.createNativeQuery("select * from sor.content_view c" +
                            " join unnest(array" + subContent.toString() + ")" +
                            " AS ppt_content_id" +
                            " on c.id = ppt_content_id" +
                            " ORDER BY " + sortingColumn + " " + sortingDirection,
                    ContentView.class).getResultList();
             result.addAll(contentViewsBstch);
        });

        logTime("Join c temp таблицей ", startTime);
        return result;
    }

    private Page<ContentView> createPageable(Pageable pageable, List<ContentView> result, Integer totalElements) {
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > result.size() ? result.size() : (start + pageable.getPageSize());

        return new PageImpl<ContentView>((result.subList(start, end)), pageable, totalElements);
    }

    public String convertCamelCaseToSnakeCase(String parse) {
        parse = parse.replaceAll("([^_A-Z])([A-Z])", "$1_$2");
        parse.toLowerCase();
        return parse;

    }
}
