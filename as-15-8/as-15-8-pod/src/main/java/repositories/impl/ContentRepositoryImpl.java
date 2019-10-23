package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.enums.Dictionary;
import model.projection.ContentView;
import model.scheme.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import repositories.ContentRepositoryCustom;
import repositories.helper.DictionaryRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom, DictionaryRepository {

    private final EntityManager em;

    /* DictionaryRepository */

    @Override
    public Dictionary getDictionaryType() {
        return Dictionary.ERDI;
    }

    @Override
    public long getCountByEffDt(Date effDt) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ContentHistory> countRoot = countQuery.from(ContentHistory.class);
        countQuery.select(cb.count(countRoot)); // to do js - count(content_id)
        countQuery.where(cb.equal(countRoot.get(ContentHistory_.endDate), effDt));
        return em.createQuery(countQuery).getSingleResult();
    }

    @Override
    public Date getUpdateDateTime(Date effDt) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Date> dateQuery = cb.createQuery(Date.class);
        Root<ContentInfo> dateRoot = dateQuery.from(ContentInfo.class);
        // to do js - join with ContentHistory
        dateQuery.select(cb.greatest(dateRoot.get(ContentInfo_.ts)));
        return em.createQuery(dateQuery).getSingleResult();
    }

    /* ContentRepositoryCustom */

    @Override
    public Page<ContentView> findByEffDtAndQuery(Date effDt, String query, Pageable pageable) {
        // to do js - filter by query

        CriteriaBuilder cb = em.getCriteriaBuilder();
        long total = getCountByEffDt(effDt);

        CriteriaQuery<ContentView> mainQuery = cb.createQuery(ContentView.class);
        Root<Content> mainRoot = mainQuery.from(Content.class);
        Join<Content, ContentHistory> historyJoin = mainRoot.join(Content_.contentHistory);
//        Join<Content, Decision> decisionJoin = mainRoot.join(Content_.decisions, JoinType.LEFT);
//        decisionJoin.on(cb.equal(decisionJoin.get(Decision_.contentVersion), historyJoin.get(ContentHistory_.contentVersion)));
//        Join<ContentHistory, AddonVersion> addonVersionJoin = historyJoin.join(ContentHistory_.addonVersion, JoinType.LEFT);
//        Join<AddonVersion, Addon> addonJoin = addonVersionJoin.join(AddonVersion_.addons);

        mainQuery.multiselect(
                mainRoot.get(Content_.id),
                mainRoot.get(Content_.erdiId),
                historyJoin.get(ContentHistory_.contentVersion).get(ContentVersion_.id),
                historyJoin.get(ContentHistory_.addonVersion).get(AddonVersion_.id)
//                decisionJoin.get(Decision_.org),
//                addonJoin.get(Addon_.infoTypeId)
        );

        mainQuery.where(cb.equal(historyJoin.get(ContentHistory_.endDate), effDt));
        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));

        TypedQuery<ContentView> typedQuery = em.createQuery(mainQuery);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ContentView> result = typedQuery.getResultList();
        return new PageImpl<>(result, pageable, total);
    }

/*
    with help as (
        select content.id                   as content_id,
               max(addon.id)                as addon_id,
               history.content_version_id   as content_version_id,
               min(resources.id)            as resource_id
        from sor.content content
                 join sor.content_history history
                      on content.id = history.content_id
                          and history.end_dt = to_date('30000101', 'YYYYMMDD')
                 join sor.addon addon
                      on history.addon_version_id = addon.addon_version_id
                 join sor.content_info info
                      on content.id = info.content_id and
                         history.content_version_id = info.content_version_id
                 join sor.content_resources resources
                      on content.id = resources.content_id and
                         history.content_version_id = info.content_version_id and
                         case
                             when info.blocktype like 'domain%' then resources.resource_type_id = 1
                             when info.blocktype = 'ip' then resource_type_id in (2, 3, 4, 5)
                             else resources.resource_type_id = 6
                         end
        group by content.id, history.content_version_id
    ) select
             help.content_id as content_id,
             res.value as resource_value,
             restype.dsc as resource_type,
             subtype.orig_id as info_type_id,
             subtype.registry_name as registry_name,
             subtype.category_name as category_name,
             subtype.violation_name as violation_name,
             decision.org as decisionOrg
    from help
    join sor.content_resources res on help.resource_id = res.id
    join sor.addon addon on help.addon_id = addon.id
    join sor.subtype subtype on addon.info_type_id = subtype.orig_id
    join sor.resource_type restype on res.resource_type_id = restype.id
    join sor.decision decision
        on help.content_id = decision.content_id and
           help.content_version_id = decision.content_version_id
    order by content_id
    limit 10;
*/

}