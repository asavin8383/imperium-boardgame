package repositories.helper;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
public class GenericRelevantQuery<T> {

    private final EntityManager em;
    private final Class<T> tClass;

    public Page<T> query(Date effDt, SingularAttribute<T, Date> effDtAttr,
                         String query, List<SingularAttribute<T, String>> likeAttrs,
                         Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> mainQuery = cb.createQuery(tClass);
        Root<T> mainRoot = mainQuery.from(tClass);
        Predicate predicate = cb.equal(mainRoot.get(effDtAttr), effDt);

        if ( !StringUtils.isEmpty(query) ) {
            String likeQuery = "%" + query.toLowerCase() + "%";
            List<Predicate> queryPredicates = new ArrayList<>(likeAttrs.size());
            for (SingularAttribute<T, String> attr : likeAttrs) {
                queryPredicates.add(cb.like(cb.lower(mainRoot.get(attr)), likeQuery));
            }
            predicate = cb.and(predicate,
                    cb.or(queryPredicates.toArray(new Predicate[0])));

        }

        mainQuery.where(predicate);
        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));
        return CriteriaHelper.createPage(em, mainQuery, pageable);
    }

    
}
