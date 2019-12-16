package repositories.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.projection.ContentView;
import model.projection.ContentView_;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import repositories.ContentViewRepositoryAdvanced;
import repositories.helper.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 02.12.2019
 */
@Repository
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentViewRepositoryAdvancedImpl implements ContentViewRepositoryAdvanced {

    private final EntityManager em;

    @Override
    public Page<ContentView> findPage(
            String id,
            List<String> categoryNames,
            List<String> decisionOrgs,
            List<String> infoTypeIds,
            List<String> registryNames,
            List<String> resourceTypes,
            String resourceValue,
            List<String> violationNames,
            String query,
            boolean random,
            Pageable pageable) {

        CriteriaQuery<ContentView> select = getCriteriaQuery(id,
                categoryNames,
                decisionOrgs,
                infoTypeIds,
                registryNames,
                resourceTypes,
                resourceValue,
                violationNames,
                query,
                random,
                pageable);

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
            Integer maxResults) {

        CriteriaQuery<ContentView> select = getCriteriaQuery(idMask,
                categoryNames,
                decisionOrgs,
                infoTypeIds,
                registryNames,
                resourceTypes,
                resourceValue,
                violationNames,
                null,
                false,
                null);

        return  CriteriaHelper.createIds(em, select, maxResults);
    }

    private CriteriaQuery<ContentView> getCriteriaQuery(String idMask,
                             List<String> categoryNames,
                             List<String> decisionOrgs,
                             List<String> infoTypeIds,
                             List<String> registryNames,
                             List<String> resourceTypes,
                             String resourceValue,
                             List<String> violationNames,
                             String query,
                             boolean random, Pageable pageable) {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ContentView> cq = criteriaBuilder.createQuery(ContentView.class);

        Root<ContentView> fromContentView = cq.from(ContentView.class);

        List<Predicate> predicates = new ArrayList<>();

        if (idMask != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.ID)), "%" + idMask.toUpperCase() + "%"));
        }

        if (categoryNames != null && categoryNames.size() > 0) {
            predicates.add(fromContentView.get(ContentView_.CATEGORY_NAME).in(categoryNames));
        }

        if (decisionOrgs != null && decisionOrgs.size() > 0) {
            predicates.add(fromContentView.get(ContentView_.DECISION_ORG).in(decisionOrgs));
        }

        if (infoTypeIds != null && infoTypeIds.size() > 0) {
            predicates.add(fromContentView.get(ContentView_.INFO_TYPE_ID).in(infoTypeIds));
        }

        if (registryNames != null && registryNames.size() > 0) {
            predicates.add(fromContentView.get(ContentView_.REGISTRY_NAME).in(registryNames));
        }

        if (resourceTypes != null && resourceTypes.size() > 0) {
            predicates.add(fromContentView.get(ContentView_.RESOURCE_TYPE).in(resourceTypes));
        }

        if (resourceValue != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.RESOURCE_VALUE)), "%" + resourceValue.toUpperCase() + "%"));
        }

        if (violationNames != null && violationNames.size() > 0) {
            predicates.add(fromContentView.get(ContentView_.VIOLATION_NAME).in(violationNames));
        }

        if (query != null) {
            predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.ID)), "%" + query.toUpperCase()),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.CATEGORY_NAME)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.DECISION_ORG)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.INFO_TYPE_ID)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.REGISTRY_NAME)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.RESOURCE_TYPE)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.RESOURCE_VALUE)), "%" + query.toUpperCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.VIOLATION_NAME)), "%" + query.toUpperCase() + "%")
                    )
            );
        }

        cq.where(predicates.toArray(new Predicate[0]));

        //Берём сортировку из Pageable
        if(!random && pageable!= null){
            cq.orderBy(QueryUtils.toOrders(pageable.getSort(), fromContentView, criteriaBuilder));
        } if (!random && pageable == null)
            return cq;
        else {
            cq.orderBy(criteriaBuilder.asc(criteriaBuilder.function("random", Double.class)));
        }
        return cq;
    }

}
