package repositories.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@RequiredArgsConstructor
public abstract class JoinCriteriaHelper<T, R> { //implements AdvancedRepositoryWithJoin<T, R> {

    protected final EntityManager em;

    public Page<T> searchFor(Class<T> clazz, R params, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(clazz);
        List<Predicate> countPredicates =
                getPredicates(countQuery, countRoot, clazz, params);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        long total = em.createQuery(countQuery).getSingleResult();

        CriteriaQuery<T> mainQuery = cb.createQuery(clazz);
        Root<T> mainRoot = mainQuery.from(clazz);
        List<Predicate> mainPredicates =
                getPredicates(mainQuery, mainRoot, clazz, params);
        mainQuery.where(mainPredicates.toArray(new Predicate[0]));
        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));

        TypedQuery<T> query = em.createQuery(mainQuery);

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<T> result = query.getResultList();
        return new PageImpl<>(result, pageable, total);
    }

    protected abstract List<Predicate> getPredicates(CriteriaQuery query, Root<T> root, Class<T> clazz, R params);

}
