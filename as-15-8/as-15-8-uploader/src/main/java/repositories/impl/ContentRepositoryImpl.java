package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import repositories.ContentRepositoryCustom;
import services.ContentService;
import utils.Utils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<ContentService.ContentView> findRelevant(Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Date endDate = Utils.getEndDate();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Content> countRoot = countQuery.from(Content.class);
        Join<Content, ContentHistory> countHstJoin = countRoot.join(Content_.contentHistory);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.equal(countHstJoin.get(ContentHistory_.endDate), endDate));
        long total = em.createQuery(countQuery).getSingleResult();

        /* MAIN */

        CriteriaQuery<ContentService.ContentView> mainQuery = cb.createQuery(ContentService.ContentView.class);
        Root<Content> mainRoot = mainQuery.from(Content.class);
        Join<Content, ContentHistory> mainHstJoin = mainRoot.join(Content_.contentHistory);
        mainQuery.multiselect(mainRoot.get(Content_.id), mainRoot.get(Content_.erdiId),
                mainHstJoin.get(ContentHistory_.contentVersion).get(ContentVersion_.id));
        mainQuery.where(cb.equal(mainHstJoin.get(ContentHistory_.endDate), endDate));
        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));

        TypedQuery<ContentService.ContentView> query = em.createQuery(mainQuery);

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<ContentService.ContentView> result = query.getResultList();
        return new PageImpl<>(result, pageable, total);
    }
}