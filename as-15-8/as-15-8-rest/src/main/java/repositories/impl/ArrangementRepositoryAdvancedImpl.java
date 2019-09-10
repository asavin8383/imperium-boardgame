package repositories.impl;

import enums.ExecutionStatus;
import model.task.Arrangement;
import model.task.Arrangement_;
import model.task.FormalTask_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import repositories.ArrangementRepositoryAdvanced;
import repositories.helpers.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */

@Repository
public class ArrangementRepositoryAdvancedImpl implements ArrangementRepositoryAdvanced {

    private EntityManager em;

    @Autowired
    public ArrangementRepositoryAdvancedImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<Arrangement> findPage(Long formalTaskId, Long id, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Arrangement> select = criteriaBuilder.createQuery(Arrangement.class);
        Root<Arrangement> fromArrangement = select.from(Arrangement.class);

        List<Predicate> predicates = new ArrayList<>();

        if (formalTaskId != null) {
            predicates.add(criteriaBuilder.equal(fromArrangement.get(Arrangement_.FORMAL_TASK).get(FormalTask_.ID), formalTaskId));
        }

        if (id != null) {
            predicates.add(criteriaBuilder.equal(fromArrangement.get(Arrangement_.ID), id));
        }
        select.where(predicates.toArray(new Predicate[0]));

        //Берём сортировку из Pageable
        select.orderBy(QueryUtils.toOrders(pageable.getSort(), fromArrangement, criteriaBuilder));

        return CriteriaHelper.createPage(em, select, pageable);

    }

    @Override
    public Page<Arrangement> findPageByStatus(ExecutionStatus status, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Arrangement> select = criteriaBuilder.createQuery(Arrangement.class);
        Root<Arrangement> fromArrangement = select.from(Arrangement.class);

        select.where(criteriaBuilder.equal(fromArrangement.get(Arrangement_.STATUS), status));

        //Берём сортировку из Pageable
        select.orderBy(QueryUtils.toOrders(pageable.getSort(), fromArrangement, criteriaBuilder));

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
