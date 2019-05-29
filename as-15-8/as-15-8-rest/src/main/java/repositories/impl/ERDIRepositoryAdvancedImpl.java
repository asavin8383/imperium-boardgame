package repositories.impl;

import model.erdi.Decision;
import model.erdi.Decision_;
import model.erdi.ERDI;
import model.erdi.ERDI_;
import model.task.ArrangementItem;
import model.task.ArrangementItem_;
import model.task.Arrangement_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import repositories.ERDIRepositoryAdvanced;
import repositories.helpers.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

//TODO Реализовать остальные фильтры

@Repository
public class ERDIRepositoryAdvancedImpl implements ERDIRepositoryAdvanced {

    private EntityManager em;

    @Autowired
    public ERDIRepositoryAdvancedImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<ERDI> findPage(Long id, Long arrangementId, String organization, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ERDI> select = criteriaBuilder.createQuery(ERDI.class);
        Root<ERDI> erdi = select.from(ERDI.class);

        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(criteriaBuilder.equal(erdi.get(ERDI_.ID), id));
        }
        if (arrangementId != null) {
            Join<ERDI, ArrangementItem> arrangementItem = erdi.join(ERDI_.ARRANGEMENT_ITEMS);
            predicates.add(criteriaBuilder.equal(arrangementItem.get(ArrangementItem_.ARRANGEMENT).get(Arrangement_.ID), arrangementId));
        }
        if(organization != null) {
            Join<ERDI, Decision> decision = erdi.join(ERDI_.DECISION_LIST);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(decision.get(Decision_.ORGANIZATION)), "%" + organization.toLowerCase() + "%"));
        }
        select.where(predicates.toArray(new Predicate[0]));

        //TODO получать сортировку из Pageable
        select.orderBy(criteriaBuilder.asc(erdi.get("id")));

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
