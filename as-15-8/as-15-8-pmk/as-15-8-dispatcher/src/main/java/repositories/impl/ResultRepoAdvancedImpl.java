package repositories.impl;

import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import model.Result_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import repositories.ResultRepoAdvanced;
import repositories.helper.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResultRepoAdvancedImpl implements ResultRepoAdvanced {

    private final EntityManager em;

    @Override
    public Page<Result> findByFilter(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResultNames,
            List<CheckUnitType> typeNames,
            String query,
            Pageable pageable) {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Result> select = criteriaBuilder.createQuery(Result.class);
        Root<Result> root = select.from(Result.class);

        List<Predicate> predicates = new ArrayList<>();

        if (arrangementId != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get(Result_.ARRANGEMENT_ID).as(String.class)), "%" + arrangementId + "%"));
        }

        if (checkUnitJobResultNames != null && checkUnitJobResultNames.size() > 0) {
            predicates.add(root.get(Result_.RESULT).in(checkUnitJobResultNames));
        }

        if (typeNames != null && typeNames.size() > 0) {
            predicates.add(root.get(Result_.CHECK_UNIT_TYPE).in(typeNames));
        }

        if (query != null) {
            predicates.add(
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get(Result_.ARRANGEMENT_ID)), "%" + query.toUpperCase()),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get(Result_.CHECK_UNIT_TYPE)), "%" + query.toUpperCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get(Result_.RESULT)), "%" + query.toUpperCase() + "%")
                )
            );
        }

        select.where(predicates.toArray(new Predicate[0]));
        select.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        return CriteriaHelper.createPage(em, select, pageable);
    }

}
