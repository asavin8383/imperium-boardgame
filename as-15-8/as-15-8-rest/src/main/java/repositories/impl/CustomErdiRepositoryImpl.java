package repositories.impl;

import model.traffic.*;
import model.traffic.projection.CustomErdiRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.StringUtils;
import repositories.CustomErdiRepositoryCustom;
import repositories.helpers.CustomErdiParams;
import repositories.helpers.JoinCriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

    public Page<CustomErdiRow> searchFor(CustomErdiParams params, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CustomErdi> countRoot = countQuery.from(CustomErdi.class);
        List<Predicate> countPredicates =
                getPredicates(countRoot, params, null);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        long total = em.createQuery(countQuery).getSingleResult();

        CriteriaQuery<CustomErdiRow> mainQuery = cb.createQuery(CustomErdiRow.class);
        Root<CustomErdi> mainRoot = mainQuery.from(CustomErdi.class);
        Join<CustomErdi, CustomErdiUnit> mainUnitJoin =
                mainRoot.join(CustomErdi_.customErdiUnits, JoinType.LEFT);
        mainQuery.multiselect(
                mainRoot.get(CustomErdi_.id),
                mainRoot.get(CustomErdi_.name),
                mainRoot.get(CustomErdi_.violation),
                mainUnitJoin.get(CustomErdiUnit_.value),
                mainUnitJoin.get(CustomErdiUnit_.type));

        Subquery<Long> subQuery = mainQuery.subquery(Long.class);
        Root<CustomErdiUnit> subRoot = subQuery.from(CustomErdiUnit.class);
        subQuery.select(cb.min(subRoot.get(CustomErdiUnit_.id)));
        subQuery.where(cb.equal(subRoot.get(CustomErdiUnit_.customErdi),
                mainRoot.get(CustomErdi_.id)));
        List<Predicate> mainPredicates = getPredicates(mainRoot, params, mainUnitJoin);
        mainPredicates.add(cb.or(cb.isNull(mainUnitJoin.get(CustomErdiUnit_.id)),
                cb.equal(mainUnitJoin.get(CustomErdiUnit_.id), subQuery)));

        mainQuery.where(mainPredicates.toArray(new Predicate[0]));
        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));

        TypedQuery<CustomErdiRow> query = em.createQuery(mainQuery);

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<CustomErdiRow> result = query.getResultList();
        return new PageImpl<>(result, pageable, total);
    }

    protected List<Predicate> getPredicates(Root<CustomErdi> root, CustomErdiParams params,
                                            Join<CustomErdi, CustomErdiUnit> unitJoin) {
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

        if (unitJoin != null && !StringUtils.isEmpty(params.getValue())) {
            Predicate namePredicate = cb.like(cb.upper(root.get("name")), '%' + value.toUpperCase() + '%');
            Predicate valuePredicate = cb.like(cb.upper(unitJoin.get("value")), '%' + value.toUpperCase() + '%');
            predicates.add(cb.or(namePredicate, valuePredicate));
        }

        // todo join only if main query !
        root.join(CustomErdi_.violation, JoinType.LEFT);
        if (params.getViolationId() != null) {
            predicates.add(cb.equal(root.get("violation"), params.getViolationId()));
        }

        return predicates;
    }

}