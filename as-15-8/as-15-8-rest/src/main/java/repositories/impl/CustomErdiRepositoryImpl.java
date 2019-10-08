package repositories.impl;

import model.traffic.*;
import org.springframework.beans.factory.annotation.Autowired;
import repositories.CustomErdiRepositoryCustom;
import repositories.helpers.CustomErdiParams;
import repositories.helpers.JoinCriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class CustomErdiRepositoryImpl extends JoinCriteriaHelper<CustomErdi, CustomErdiParams>
        implements CustomErdiRepositoryCustom {

    public CustomErdiRepositoryImpl(@Autowired EntityManager em) {
        super(em);
    }

    @Override
    protected List<Predicate> getPredicates(CriteriaQuery query, Root<CustomErdi> root,
                                            Class<CustomErdi> clazz, CustomErdiParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();

        if (! params.isReturnAll() && params.getErdiTrafficUnitId() != null) {
            Join<CustomErdi, ErdiTrafficUnit> trafficUnitJoin =
                    root.join(CustomErdi_.erdiTrafficUnits, JoinType.LEFT);
            predicates.add(cb.equal(
                    trafficUnitJoin.get("id"), params.getErdiTrafficUnitId()));
        }

        if (! params.isReturnAll() && params.getSearchTrafficUnitId() != null) {
            Join<CustomErdi, SearchQueryTrafficUnit> trafficUnitJoin =
                    root.join(CustomErdi_.searchQueryTrafficUnits, JoinType.LEFT);
            predicates.add(cb.equal(
                    trafficUnitJoin.get("id"), params.getSearchTrafficUnitId()));
        }

        String value = params.getValue();

        // todo params.getResourceTypeId()

        if (value != null && value.length() > 0) {
            Join<CustomErdi, CustomErdiUnit> customUnitJoin =
                    root.join(CustomErdi_.customErdiUnits, JoinType.LEFT);

            Predicate namePredicate = cb.like(cb.upper(root.get("name")), '%' + value.toUpperCase() + '%');
            Predicate valuePredicate = cb.like(cb.upper(customUnitJoin.get("value")), '%' + value.toUpperCase() + '%');
            predicates.add(cb.or(namePredicate, valuePredicate));
        }

        if (params.getViolationId() != null) {
            predicates.add(cb.equal(root.get("violation"), params.getViolationId()));
        }

        return predicates;
    }

}