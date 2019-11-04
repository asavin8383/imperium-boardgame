package repositories.impl;

import model.enums.TrafficUnitType;
import model.traffic.SearchQueryTrafficUnit;
import model.traffic.SearchQueryTrafficUnit_;
import model.traffic.Traffic_;
import org.springframework.beans.factory.annotation.Autowired;
import repositories.SearchQueryTrafficUnitRepositoryCustom;
import repositories.helpers.JoinCriteriaHelper;
import repositories.helpers.SearchTemplateParams;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
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

        predicates.add(cb.like(root.get(SearchQueryTrafficUnit_.name), '%' + TrafficUnitType.TEMPLATE.toString()));

        if (params.getTrafficId() != null) {
            Path<Long> pathTrafficId = root.get(SearchQueryTrafficUnit_.traffic).get(Traffic_.id);
            if (params.isContainsInTraffic()) {
                predicates.add(cb.equal(pathTrafficId, params.getTrafficId()));
            } else {
                predicates.add(cb.or(cb.not(cb.equal(pathTrafficId,
                        params.getTrafficId())), cb.isNull(pathTrafficId)));
            }
        }

        if (params.getTemplate() != null && params.getTemplate().length() > 0) {
            predicates.add(cb.like(cb.upper(root.get("queryPattern")),
                    '%' + params.getTemplate().toUpperCase() + '%'));
        }

        return predicates;
    }

}