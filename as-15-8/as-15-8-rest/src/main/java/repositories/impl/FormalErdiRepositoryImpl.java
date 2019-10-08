package repositories.impl;

import model.erdi.ContentResource;
import model.erdi.FormalErdi;
import model.erdi.FormalErdi_;
import model.traffic.ErdiTrafficUnit;
import model.traffic.SearchQueryTrafficUnit;
import repositories.FormalErdiRepositoryCustom;
import repositories.helpers.FormalErdiParams;
import repositories.helpers.JoinCriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class FormalErdiRepositoryImpl extends JoinCriteriaHelper<FormalErdi, FormalErdiParams>
        implements FormalErdiRepositoryCustom {

    public FormalErdiRepositoryImpl(EntityManager em) {
        super(em);
    }

    @Override
    protected List<Predicate> getPredicates(CriteriaQuery query, Root<FormalErdi> root,
                                            Class<FormalErdi> clazz, FormalErdiParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();
        String value = params.getValue();

        if (! params.isReturnAll() && params.getErdiTrafficUnitId() != null) {
            Join<FormalErdi, ErdiTrafficUnit> trafficUnitJoin =
                    root.join(FormalErdi_.erdiTrafficUnits, JoinType.LEFT);
            predicates.add(cb.equal(
                    trafficUnitJoin.get("id"), params.getErdiTrafficUnitId()));
        }

        if (! params.isReturnAll() && params.getSearchTrafficUnitId() != null) {
            Join<FormalErdi, SearchQueryTrafficUnit> trafficUnitJoin =
                    root.join(FormalErdi_.searchQueryTrafficUnits, JoinType.LEFT);
            predicates.add(cb.equal(
                    trafficUnitJoin.get("id"), params.getSearchTrafficUnitId()));
        }

        if (params.getResourceTypeId() != null || value != null && value.length() > 0) {
            Join<FormalErdi, ContentResource> contentResourceJoin =
                    root.join(FormalErdi_.contentResources, JoinType.LEFT);

            if (value != null) {
                predicates.add(cb.like(
                        cb.upper(contentResourceJoin.get("value")),
                        '%' + value.toUpperCase() + '%'));
            }

            if (params.getResourceTypeId() != null) {
                predicates.add(cb.equal(
                        contentResourceJoin.get("resourceType"), params.getResourceTypeId()));
            }
        }
        return predicates;
    }

}