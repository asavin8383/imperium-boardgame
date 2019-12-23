package repositories.impl;

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
            boolean random,
            Pageable pageable,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        initBasicArguments(idMask, categoryNames, decisionOrgs, infoTypeIds, registryNames, resourceTypes, resourceValue,
                violationNames,
                startTime,
                endTime);

        CriteriaQuery<ContentView> select = getCriteriaQuery(random, pageable, query);
        return CriteriaHelper.createPage(em, select, pageable);
    }

    @Override
    public List<List<Long>> findIds(
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
            LocalDateTime endTime) {

        initBasicArguments(idMask, categoryNames, decisionOrgs, infoTypeIds, registryNames, resourceTypes, resourceValue, violationNames, startTime, endTime);

        CriteriaQuery<ContentView> select = getCriteriaQuery();
        return  CriteriaHelper.createIds(em, select, maxResults);
    }

    private void initBasicArguments(String idMask, List<String> categoryNames, List<String> decisionOrgs, List<String> infoTypeIds, List<String> registryNames, List<String> resourceTypes, String resourceValue, List<String> violationNames,  LocalDateTime startTime,  LocalDateTime endTime) {
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
    }

    private CriteriaQuery<ContentView> getCriteriaQuery(Boolean random, Pageable pageable, String query) {

        CriteriaQuery<ContentView> cq = createCriteriaQuery();

        rootContentView = cq.from(ContentView.class);

        List<Predicate> predicates = new ArrayList<>();

        createPredicatesFromMasks(predicates);
        createPredicatesFromQuery(query, predicates);

        cq.where(predicates.toArray(new Predicate[0]));

        //Берём сортировку из Pageable
        if(!random && pageable!= null){
            cq.orderBy(QueryUtils.toOrders(pageable.getSort(), rootContentView, criteriaBuilder));
        } else {
            cq.orderBy(criteriaBuilder.asc(criteriaBuilder.function("random", Double.class)));
        }
        return cq;
    }
    private CriteriaQuery<ContentView> getCriteriaQuery() {

        CriteriaQuery<ContentView> cq = createCriteriaQuery();

        rootContentView = cq.from(ContentView.class);

        List<Predicate> predicates = new ArrayList<>();
        createPredicatesFromMasks(predicates);

        cq.where(predicates.toArray(new Predicate[0]));

        return cq;
    }

    private CriteriaQuery<ContentView> createCriteriaQuery() {
        criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ContentView> cq = criteriaBuilder.createQuery(ContentView.class);
        return cq;
    }

    private List<Predicate> createPredicatesFromMasks(List<Predicate> predicates) {

        if (idMask != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.ID)), "%" + idMask.toUpperCase() + "%"));
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

        if (startTime != null && endTime != null) {
            predicates.add(criteriaBuilder.between(rootContentView.get(ContentView_.INCLUDE_TIME), startTime, endTime));
        }

        return predicates;
    }

    private List<Predicate> createPredicatesFromQuery(String query, List<Predicate> predicates) {
        if (query != null) {
            predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.upper(rootContentView.get(ContentView_.ID)), "%" + query.toUpperCase()),
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
