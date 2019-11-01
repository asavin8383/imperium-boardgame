package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.enums.Dictionary;
import model.scheme.ContentHistory;
import model.scheme.ContentHistory_;
import model.scheme.ContentInfo;
import model.scheme.ContentInfo_;
import repositories.helper.DictionaryRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements DictionaryRepository {

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

}