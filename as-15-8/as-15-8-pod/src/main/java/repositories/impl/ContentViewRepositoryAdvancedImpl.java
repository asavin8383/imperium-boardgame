package repositories.impl;

import com.rometools.utils.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.projection.ContentView;
import model.projection.ContentView_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import repositories.ContentViewRepositoryAdvanced;
import repositories.helper.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by san
 * Date: 02.12.2019
 */
@Repository
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentViewRepositoryAdvancedImpl implements ContentViewRepositoryAdvanced {

    private final EntityManager em;
    private String idMask;
    private List<String> categoryNames;
    private List<String> decisionOrgs;
    private List<String> infoTypeIds;
    private List<String> registryNames;
    private List<String> resourceTypes;
    private String resourceValue;
    private List<String> violationNames;

    private CriteriaBuilder criteriaBuilder;
    private Root<ContentView> rootContentView;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean random;
    private Pageable pageable;
    private Long visitorsCntRussiaMin;
    private Long visitorsCntRussiaMax;
    private Long visitorsCntWorldMin;
    private Long visitorsCntWorldMax;
    private String query;

    @Override
    public Page<ContentView> findPage(
            String idMask,
            List<String> categoryNames,
            List<String> decisionOrgs,
            List<String> infoTypeIds,
            List<String> registryNames,
            List<String> resourceTypes,
            String resourceValue,
            List<String> violationNames,
            String query,
            Boolean random,
            Pageable pageable,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long visitorsCntRussiaMin,
            Long visitorsCntRussiaMax,
            Long visitorsCntWorldMin,
            Long visitorsCntWorldMax) {

        initBasicArguments(idMask, categoryNames, decisionOrgs, infoTypeIds, registryNames, resourceTypes, resourceValue,
                violationNames, startTime, endTime, random, pageable, visitorsCntRussiaMin, visitorsCntRussiaMax,
                visitorsCntWorldMin, visitorsCntWorldMax, query);

        CriteriaQuery<ContentView> select = configurateCriteriaQuery();
        return CriteriaHelper.createPage(em, select, pageable);
    }

    @Override
    public List<Long> findIds(
            String idMask,
            List<String> categoryNames,
            List<String> decisionOrgs,
            List<String> infoTypeIds,
            List<String> registryNames,
            List<String> resourceTypes,
            String resourceValue,
            List<String> violationNames,
            Integer maxResults,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Boolean random,
            Pageable pageable,
            Long visitorsCntRussiaMin,
            Long visitorsCntRussiaMax,
            Long visitorsCntWorldMin,
            Long visitorsCntWorldMax) {

        initBasicArguments(idMask, categoryNames, decisionOrgs, infoTypeIds, registryNames, resourceTypes, resourceValue,
                violationNames, startTime, endTime, random, pageable, visitorsCntRussiaMin, visitorsCntRussiaMax,
                visitorsCntWorldMin, visitorsCntWorldMax, null);

        CriteriaQuery<ContentView> select = configurateCriteriaQuery();
        return  CriteriaHelper.createIds(em, select, maxResults);
    }

    private void initBasicArguments(String idMask, List<String> categoryNames, List<String> decisionOrgs, List<String> infoTypeIds,
                                    List<String> registryNames, List<String> resourceTypes, String resourceValue, List<String> violationNames,
                                    LocalDateTime startTime, LocalDateTime endTime, Boolean random, Pageable pageable,
                                    Long visitorsCntRussiaMin, Long visitorsCntRussiaMax, Long visitorsCntWorldMin, Long visitorsCntWorldMax, String query) {
        this.idMask = idMask;
        this.categoryNames = categoryNames;
        this.decisionOrgs = decisionOrgs;
        this.infoTypeIds = infoTypeIds;
        this.registryNames = registryNames;
        this.resourceTypes = resourceTypes;
        this.resourceValue = resourceValue;
        this.violationNames = violationNames;
        this.startTime = startTime;
        this.endTime = endTime;
        this.random = random;
        this.pageable = pageable;
        this.visitorsCntRussiaMin =  visitorsCntRussiaMin;
        this.visitorsCntRussiaMax =  visitorsCntRussiaMax;
        this.visitorsCntWorldMin = visitorsCntWorldMin;
        this.visitorsCntWorldMax = visitorsCntWorldMax;
        this.query = query;
    }

    private CriteriaQuery<ContentView> configurateCriteriaQuery() {

        CriteriaQuery<ContentView> cq = createCriteriaQuery();
        rootContentView = cq.from(ContentView.class);

        List<Predicate> predicates = new ArrayList<>();

        if(!Strings.isEmpty(query)) {
            createPredicatesFromQuery(query, predicates);
        } else createPredicatesFromMasks(predicates);

        cq.where(predicates.toArray(new Predicate[0]));

        orderByOrRandom(cq);
        return cq;
    }

    private void orderByOrRandom(CriteriaQuery cq) {
        if (pageable!= null) {
            cq.orderBy(QueryUtils.toOrders(pageable.getSort(), rootContentView, criteriaBuilder));
        }

        if (random != null && random){
            cq.orderBy(criteriaBuilder.asc(criteriaBuilder.function("random", Double.class)));
        }

    }

    private CriteriaQuery<ContentView> createCriteriaQuery() {
        criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ContentView> cq = criteriaBuilder.createQuery(ContentView.class);
        return cq;
    }

    private List<Predicate> createPredicatesFromMasks(List<Predicate> predicates) {

        if (idMask != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.ID).as(String.class)), "%" + idMask.toUpperCase() + "%"));
        }

        if (categoryNames != null && categoryNames.size() > 0) {
            predicates.add(rootContentView.get(ContentView_.CATEGORY_NAME).in(categoryNames));
        }

        if (decisionOrgs != null && decisionOrgs.size() > 0) {
            predicates.add(rootContentView.get(ContentView_.DECISION_ORG).in(decisionOrgs));
        }

        if (infoTypeIds != null && infoTypeIds.size() > 0) {
            predicates.add(rootContentView.get(ContentView_.INFO_TYPE_ID).in(infoTypeIds));
        }

        if (registryNames != null && registryNames.size() > 0) {
            predicates.add(rootContentView.get(ContentView_.REGISTRY_NAME).in(registryNames));
        }

        if (resourceTypes != null && resourceTypes.size() > 0) {
            predicates.add(rootContentView.get(ContentView_.RESOURCE_TYPE).in(resourceTypes));
        }

        if (resourceValue != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.RESOURCE_VALUE)), "%" + resourceValue.toUpperCase() + "%"));
        }

        if (violationNames != null && violationNames.size() > 0) {
            predicates.add(rootContentView.get(ContentView_.VIOLATION_NAME).in(violationNames));
        }

        if (startTime != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(rootContentView.get(ContentView_.INCLUDE_TIME), startTime));
        }

        if (endTime != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(rootContentView.get(ContentView_.INCLUDE_TIME), endTime));
        }

        if (visitorsCntRussiaMin != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(rootContentView.get(ContentView_.VISITORS_CNT_RUSSIA), visitorsCntRussiaMin));
        }

        if (visitorsCntRussiaMax != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(rootContentView.get(ContentView_.VISITORS_CNT_RUSSIA), visitorsCntRussiaMax));
        }

        if (visitorsCntWorldMin != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(rootContentView.get(ContentView_.VISITORS_CNT_WORLD), visitorsCntWorldMin));
        }

        if (visitorsCntWorldMax != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(rootContentView.get(ContentView_.VISITORS_CNT_WORLD), visitorsCntWorldMax));
        }

        return predicates;
    }

    private List<Predicate> createPredicatesFromQuery(String query, List<Predicate> predicates) {
        if (query != null) {
            predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.ID).as(String.class)), "%" + query.toUpperCase()),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.CATEGORY_NAME)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.DECISION_ORG)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.INFO_TYPE_ID)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.REGISTRY_NAME)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.RESOURCE_TYPE)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.RESOURCE_VALUE)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.VIOLATION_NAME)), "%" + query.toUpperCase() + "%")
                    )
            );
        }
        return predicates;
    }

}
