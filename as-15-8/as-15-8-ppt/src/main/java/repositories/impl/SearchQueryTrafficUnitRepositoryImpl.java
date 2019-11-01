package repositories.impl;

import model.enums.TrafficUnitType;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import repositories.SearchQueryTrafficUnitRepositoryCustom;
import repositories.helpers.JoinCriteriaHelper;
import repositories.helpers.SearchTemplateParams;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SearchQueryTrafficUnitRepositoryImpl extends JoinCriteriaHelper<SearchQueryTrafficUnit, SearchTemplateParams>
        implements SearchQueryTrafficUnitRepositoryCustom {

    public SearchQueryTrafficUnitRepositoryImpl(@Autowired EntityManager em) {
        super(em);
    }

    @Override
    protected List<Predicate> getPredicates(CriteriaQuery query, Root<SearchQueryTrafficUnit> root,
                                            Class<SearchQueryTrafficUnit> clazz, SearchTemplateParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.like(root.get("name"), '%' + TrafficUnitType.TEMPLATE.toString()));

        if (params.getTrafficUnitId() != null) {

            if (params.isContainsInTraffic()) {
                predicates.add(cb.equal(root.get("traffic.id"), params.getTrafficUnitId()));
            } else {
                predicates.add(cb.or(
                        cb.not(cb.equal(root.get("traffic.id"), params.getTrafficUnitId())),
                        cb.isNull(root.get("traffic.id"))
                ));
            }
        }

        if (params.getTemplate() != null && params.getTemplate().length() > 0) {
            predicates.add(cb.like(cb.upper(root.get("queryPattern")),
                    '%' + params.getTemplate().toUpperCase() + '%'));
        }

        return predicates;
    }

}