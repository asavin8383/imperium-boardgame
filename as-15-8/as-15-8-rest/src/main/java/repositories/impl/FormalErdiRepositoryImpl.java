package repositories.impl;

import model.erdi.ContentResource;
import model.erdi.FormalErdi;
import model.erdi.FormalErdi_;
import model.traffic.ErdiTrafficUnit;
import model.traffic.jointable.ErdiTrafficUnitContent;
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

        if (params.getTrafficUnitId() != null) {

            if (params.isBelongsTo()) {
                Join<FormalErdi, ErdiTrafficUnit> trafficUnitJoin =
                        root.join(FormalErdi_.erdiTrafficUnits, JoinType.LEFT);
                predicates.add(cb.equal(
                        trafficUnitJoin.get("id"), params.getTrafficUnitId()));
            } else {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<ErdiTrafficUnitContent> subRoot = sub.from(ErdiTrafficUnitContent.class);
                sub.select(subRoot.get("id"));
                sub.where(cb.and(
                        cb.equal(subRoot.get("trafficUnitId"), params.getTrafficUnitId()),
                        cb.equal(subRoot.get("contentId"), root.get("id"))));
                predicates.add(cb.not(cb.exists(sub)));
            }
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