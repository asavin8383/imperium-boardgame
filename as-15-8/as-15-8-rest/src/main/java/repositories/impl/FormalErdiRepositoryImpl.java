package repositories.impl;

import model.sor.*;
import model.traffic.ErdiTrafficUnit;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import repositories.FormalErdiRepositoryCustom;
import repositories.helpers.FormalErdiParams;
import repositories.helpers.JoinCriteriaHelper;
import utils.SorUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.LinkedList;
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

    @Override
    public Page<FormalErdi> findRelevant(String entryTypeId, String urgencyTypeId, String blockType,
                                         String value, Integer resourceTypeId, Pageable pageable) {
        /* COUNT */
        String aliasHistory = "hst";
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FormalErdi> countRoot = countQuery.from(FormalErdi.class);
        Join<FormalErdi, ContentHistory> countHstJoin = countRoot.join(FormalErdi_.contentHistory);
        countHstJoin.alias(aliasHistory);

        Predicate relevant = cb.equal(countHstJoin.get(ContentHistory_.endDate), SorUtils.getEndDate());

        countQuery.select(cb.count(countRoot));
        countQuery.where(relevant);
        long total = em.createQuery(countQuery).getSingleResult();

        /* MAIN */

        CriteriaQuery<FormalErdi> mainQuery = cb.createQuery(FormalErdi.class);
        Root<FormalErdi> mainRoot = mainQuery.from(FormalErdi.class);
        Join<FormalErdi, ContentHistory> mainHstJoin = mainRoot.join(FormalErdi_.contentHistory);
        mainHstJoin.alias(aliasHistory);

        /*Join<FormalErdi, ContentInfo> infoJoin = mainRoot.join(FormalErdi_.contentInfo);
        infoJoin.on(cb.equal(infoJoin.get(ContentInfo_.content), FormalErdi_.id));
        infoJoin.on(cb.equal(infoJoin.get(ContentInfo_.contentVersionId),
                mainHstJoin.get(ContentHistory_.contentVersionId)));

        Join<ContentInfo, EntryType> etJoin = infoJoin.join(ContentInfo_.entryType, JoinType.LEFT);
        Join<ContentInfo, EntryType> utJoin = infoJoin.join(ContentInfo_.urgencyType, JoinType.LEFT);

        Join<FormalErdi, ContentResource> resJoin = mainRoot.join(FormalErdi_.contentResources);
        resJoin.on(cb.equal(resJoin.get(ContentResource_.content), FormalErdi_.id));
        resJoin.on(cb.equal(resJoin.get(ContentResource_.contentVersionId),
                mainHstJoin.get(ContentHistory_.contentVersionId)));

        Join<ContentResource, ResourceType> rtJoin = resJoin.join(ContentResource_.resourceType);*/

        List<Predicate> predicates = new LinkedList<>();
        predicates.add(relevant);

        /*Predicate urlType = cb.and(
                cb.or(
                        cb.isNull(infoJoin.get(ContentInfo_.blockType)),
                        cb.equal(infoJoin.get(ContentInfo_.blockType), SorUtils.BLOCK_TYPE_DEFAULT)
                ),
                cb.equal(rtJoin.get(ResourceType_.description), ResourceType.Description.url));
        Predicate domainType = cb.and(
                cb.like(infoJoin.get(ContentInfo_.blockType), SorUtils.BLOCK_TYPE_DOMAIN + "%"),
                cb.equal(rtJoin.get(ResourceType_.description), ResourceType.Description.domain)
        );
        Predicate ipType = cb.and(
                cb.equal(infoJoin.get(ContentInfo_.blockType), SorUtils.BLOCK_TYPE_IP),
                cb.like(rtJoin.get(ResourceType_.description).as(String.class), ResourceType.Description.ip + "%")
        );

        predicates.add(cb.or(urlType, domainType, ipType));

        mainQuery.multiselect();*/
        mainQuery.where(predicates.toArray(new Predicate[0]));
        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));

        TypedQuery<FormalErdi> query = em.createQuery(mainQuery);

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<FormalErdi> result = query.getResultList();
        return new PageImpl<>(result, pageable, total);
    }
}