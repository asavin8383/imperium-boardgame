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
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ContentView> select = criteriaBuilder.createQuery(ContentView.class);
        Root<ContentView> fromContentView = select.from(ContentView.class);

        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromContentView.get(ContentView_.ID)), "%" + id.toUpperCase() + "%"));
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

        select.where(predicates.toArray(new Predicate[0]));

        //Берём сортировку из Pageable
        if(!random){
            select.orderBy(QueryUtils.toOrders(pageable.getSort(), fromContentView, criteriaBuilder));
        } else {
           select.orderBy(criteriaBuilder.asc(criteriaBuilder.function("random", Double.class)));
        }

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
