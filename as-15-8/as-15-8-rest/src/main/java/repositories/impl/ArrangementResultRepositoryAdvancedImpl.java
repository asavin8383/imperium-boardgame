package repositories.impl;

import checkUnits.CheckUnitType;
import model.result.ArrangementResult;
import model.result.ArrangementResult_;
import model.task.Arrangement_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import repositories.ArrangementResultRepositoryAdvanced;
import repositories.helpers.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 */
@Repository
public class ArrangementResultRepositoryAdvancedImpl implements ArrangementResultRepositoryAdvanced {

    private EntityManager em;

    @Autowired
    public ArrangementResultRepositoryAdvancedImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<ArrangementResult> findPage(Long id, Long arrangementId, String checkUnitValue, Pageable pageable, CheckUnitType checkUnitType) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ArrangementResult> select = criteriaBuilder.createQuery(ArrangementResult.class);
        Root<ArrangementResult> arrangementResult = select.from(ArrangementResult.class);

        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(criteriaBuilder.equal(arrangementResult.get(ArrangementResult_.ID), id));
        }
        if (arrangementId != null) {
            predicates.add(criteriaBuilder.equal(arrangementResult.get(ArrangementResult_.ARRANGEMENT).get(Arrangement_.ID), arrangementId));
        }
        if(checkUnitValue != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(arrangementResult.get(ArrangementResult_.CHECK_UNIT_VALUE)), "%" + checkUnitValue.toLowerCase() + "%"));
        }
        if(checkUnitType != null) {
            predicates.add(criteriaBuilder.equal(arrangementResult.get(ArrangementResult_.CHECK_UNIT_TYPE), checkUnitType));
        }

        select.where(predicates.toArray(new Predicate[0]));

        //TODO получать сортировку из Pageable
        select.orderBy(criteriaBuilder.asc(arrangementResult.get(ArrangementResult_.ID)));

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
