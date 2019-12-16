package repositories.helper;

//import org.apache.poi.ss.formula.functions.T;
import model.projection.ContentView;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */
public class CriteriaHelper {

    private static int subListSize = 1000;

    public static <T> PageImpl<T> createPage(EntityManager em, CriteriaQuery<T> select, Pageable pageable) {
        TypedQuery<T> query = em.createQuery(select);
        long total = countRowsCount(em, select);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<T> models = query.getResultList();
        return new PageImpl<>(models, pageable, total);
    }

    public static List<List<Long>> createIds(EntityManager em, CriteriaQuery<ContentView> select, Integer maxResults) {
    //public static List<List<Long>> createIds(EntityManager em, CriteriaQuery<ContentView> select) {
        TypedQuery<ContentView> query = em.createQuery(select);

        if (maxResults != null)
            query.setMaxResults(maxResults);

        List<ContentView> models = query.getResultList();

        List<Long> idList = getIdList(models);
        List<List<Long>> result = packIdListToList(idList);

        return result;
    }

    private static List<Long> getIdList(List<ContentView> models) {
        return models.stream().map(x -> Long.parseLong(x.getId())).collect(Collectors.toList());
    }

    private static List<List<Long>> packIdListToList(List<Long> idList) {
        // результат работы метода сделан для flux и hystrix
        final AtomicInteger counter = new AtomicInteger();

        List<List<Long>> result = new ArrayList<>(idList.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / subListSize))
                .values());
        return result;
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
