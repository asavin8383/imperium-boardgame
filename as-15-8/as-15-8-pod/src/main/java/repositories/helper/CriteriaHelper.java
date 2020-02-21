package repositories.helper;

//import org.apache.poi.ss.formula.functions.T;

import model.projection.ContentView;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */
public class CriteriaHelper {

    public static <T> PageImpl<T> createPage(EntityManager em, CriteriaQuery<T> select, Pageable pageable) {
        return createPage(em, select, pageable, null);
    }

    public static <T> PageImpl<T> createPage(EntityManager em, CriteriaQuery<T> select, Pageable pageable, Integer size) {
        TypedQuery<T> query = em.createQuery(select);
        long total;
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        if (size != null) {
            total = size;
            query.setMaxResults(size);
        } else {
            total = countRowsCount(em, select);
            query.setMaxResults(pageable.getPageSize());
        }

        List<T> models = query.getResultList();
        return new PageImpl<>(models, pageable, total);
    }

    public static List<Long> createIds(EntityManager em, CriteriaQuery<ContentView> select, Integer maxResults) {
        TypedQuery<ContentView> query = em.createQuery(select);

        if (maxResults != null)
            query.setMaxResults(maxResults);

        List<ContentView> models = query.getResultList();

        return getIdList(models);
    }

    private static List<Long> getIdList(List<ContentView> models) {
        return models.stream().map(ContentView::getId).collect(Collectors.toList());
    }

    private static <T>Long countRowsCount(EntityManager em, CriteriaQuery<T> select) {
        CriteriaQuery<Long> countQuery = em.getCriteriaBuilder().createQuery(Long.class);
        Root<T> entity_ = countQuery.from(select.getResultType());
        countQuery.select(em.getCriteriaBuilder().count(entity_));
        Predicate restriction = select.getRestriction();
        if (restriction != null) {
            countQuery.where(restriction); // Copy restrictions
        }

        //getQuery(em, select);
        return em.createQuery(countQuery).getSingleResult();
    }
}
