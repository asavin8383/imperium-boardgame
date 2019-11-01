package utils;

import liquibase.util.StringUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import model.projection.ContentView;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;

@UtilityClass
public class ContentViewSpecifications {

    private static final String[] SEARCH_QUERY_COLUMNS = {"resourceValue", "resourceType", "decisionOrg"};

    public static Specification<ContentView> containsQueryString(String query) {

        return (Specification<ContentView>) (root, criteriaQuery, criteriaBuilder) ->
                predicateContainsQuery(criteriaBuilder, root, query);
    }

    public static <T> Specification<ContentView> containsInTrafficUnit(String query, String joinColumn,
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
                    criteriaBuilder.equal(subRoot.get("contentView").get("contentId"),
                            root.get("contentId")),
                    criteriaBuilder.equal(subRoot.get("trafficUnitId"), trafficUnitId)
            );
            Predicate predicate = criteriaBuilder.not(criteriaBuilder.exists(subQuery));

            return StringUtils.isEmpty(query) ? predicate : criteriaBuilder.and(
                    predicate, predicateContainsQuery(criteriaBuilder, root, query));
        };
    }

    private Predicate predicateContainsQuery(CriteriaBuilder cb, Root<ContentView> root, String query) {
        String likeQuery = "%" + query + "%";

        Predicate[] likePredicates = Arrays.stream(SEARCH_QUERY_COLUMNS)
                .map(column -> cb.like(cb.lower(root.get(column)), likeQuery))
                .toArray(Predicate[]::new);

        return cb.or(likePredicates);
    }
}
