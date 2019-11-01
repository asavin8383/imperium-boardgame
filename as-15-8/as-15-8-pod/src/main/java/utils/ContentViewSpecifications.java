package utils;

import liquibase.util.StringUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import model.projection.ContentView;
import model.projection.ContentView_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ContentViewSpecifications {

    private static final List<SingularAttribute<ContentView, String>> SEARCH_QUERY_COLUMNS;
    static {
        SEARCH_QUERY_COLUMNS = new ArrayList<>(3);
        SEARCH_QUERY_COLUMNS.add(ContentView_.resourceValue);
        SEARCH_QUERY_COLUMNS.add(ContentView_.resourceType);
        SEARCH_QUERY_COLUMNS.add(ContentView_.decisionOrg);
    }

    public static Specification<ContentView> containsQueryString(String query) {

        return (Specification<ContentView>) (root, criteriaQuery, criteriaBuilder) ->
                predicateContainsQuery(criteriaBuilder, root, query);
    }

    public static <T> Specification<ContentView> containsInTrafficUnit(String query,
                                                                       String joinColumn,
                                                                       @NonNull Long trafficUnitId) {
        return (Specification<ContentView>) (root, criteriaQuery, criteriaBuilder) -> {
            Join<ContentView, T> join = root.join(joinColumn);
            Predicate predicate = criteriaBuilder.equal(
                    join.get("trafficUnitId"), trafficUnitId);

            return StringUtils.isEmpty(query) ? predicate : criteriaBuilder.and(
                    predicate, predicateContainsQuery(criteriaBuilder, root, query));
        };
    }

    public static <T> Specification<ContentView> notContainsInTrafficUnit(String query,
                                                                          Long trafficUnitId,
                                                                          Class<T> clazz) {
        return (Specification<ContentView>) (root, criteriaQuery, criteriaBuilder) -> {
            Subquery<T> subQuery = criteriaQuery.subquery(clazz);
            Root<T> subRoot = subQuery.from(clazz);
            subQuery.select(subRoot.get("id"));
            subQuery.where(
                    criteriaBuilder.equal(subRoot.get("contentView").get("id"), root.get("id")),
                    criteriaBuilder.equal(subRoot.get("trafficUnitId"), trafficUnitId)
            );
            Predicate predicate = criteriaBuilder.not(criteriaBuilder.exists(subQuery));

            return StringUtils.isEmpty(query) ? predicate : criteriaBuilder.and(
                    predicate, predicateContainsQuery(criteriaBuilder, root, query));
        };
    }

    private Predicate predicateContainsQuery(CriteriaBuilder cb, Root<ContentView> root, String query) {
        String likeQuery = "%" + query + "%";

        Predicate[] likePredicates = SEARCH_QUERY_COLUMNS.stream()
                .map(column -> cb.like(cb.lower(root.get(column)), likeQuery))
                .toArray(Predicate[]::new);

        return cb.or(likePredicates);
    }
}
