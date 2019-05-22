package repositories.helpers;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */
public class CriteriaHelper {

    public static <T> PageImpl<T> createPage(EntityManager em, CriteriaQuery<T> select, Pageable pageable){
        TypedQuery<T> query = em.createQuery(select);
        int totalRows = query.getResultList().size();
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<T> models = query.getResultList();

        return new PageImpl<>(models, pageable, totalRows);
    }
}
