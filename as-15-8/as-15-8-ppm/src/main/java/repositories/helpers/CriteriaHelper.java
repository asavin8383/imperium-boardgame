package repositories.helpers;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class CriteriaHelper {
    public static <T> PageImpl<T> createPage(EntityManager em, CriteriaQuery<T> select, Pageable pageable){
        TypedQuery<T> query = em.createQuery(select);
        //*******Считаем общее количество строк********
        CriteriaQuery<Long> countQuery = em.getCriteriaBuilder().createQuery(Long.class);
        Root<T> entity_ = countQuery.from(select.getResultType());
        countQuery.select(em.getCriteriaBuilder().count(entity_));
        Predicate restriction = select.getRestriction();
        if (restriction != null) {
            countQuery.where(restriction); // Copy restrictions
        }
        long total = em.createQuery(countQuery).getSingleResult();
        //*********************************************
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<T> models = query.getResultList();

        return new PageImpl<>(models, pageable, total);
    }
}
