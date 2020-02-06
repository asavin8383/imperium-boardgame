package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryPattern_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import repositories.SearchQueryPatternRepoAdvanced;
import repositories.helpers.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by san
 * Date: 06.02.2020
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SearchQueryPatternRepoAdvancedImpl implements SearchQueryPatternRepoAdvanced {

    private final EntityManager em;

    @Override
    public Page<SearchQueryPattern> findPage(Long id, String pattern, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<SearchQueryPattern> select = criteriaBuilder.createQuery(SearchQueryPattern.class);
        Root<SearchQueryPattern> fromSearchQueryPattern = select.from(SearchQueryPattern.class);

        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromSearchQueryPattern.get(SearchQueryPattern_.ID).as(String.class)), "%" + id.toString() + "%"));
        }
        if (pattern != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromSearchQueryPattern.get(SearchQueryPattern_.QUERY_PATTERN)), "%" + pattern.toUpperCase() + "%"));
        }

        select.where(predicates.toArray(new Predicate[0]));

        select.orderBy(QueryUtils.toOrders(pageable.getSort(), fromSearchQueryPattern, criteriaBuilder));

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
