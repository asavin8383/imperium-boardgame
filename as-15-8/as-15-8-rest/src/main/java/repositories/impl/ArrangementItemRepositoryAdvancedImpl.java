package repositories.impl;

import model.task.ArrangementItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import repositories.ArrangementItemRepositoryAdvanced;
import repositories.helpers.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
@Repository
public class ArrangementItemRepositoryAdvancedImpl implements ArrangementItemRepositoryAdvanced {

    private EntityManager em;

    @Autowired
    public ArrangementItemRepositoryAdvancedImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<ArrangementItem> findPage(Long arrangementId, Long id, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ArrangementItem> select = criteriaBuilder.createQuery(ArrangementItem.class);
        Root<ArrangementItem> fromArrangementItem = select.from(ArrangementItem.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(fromArrangementItem.get("arrangement").get("id"), arrangementId));

        if (id != null) {
            predicates.add(criteriaBuilder.equal(fromArrangementItem.get("id"), id));
        }

        select.where(predicates.toArray(new Predicate[0]));

        //TODO получать сортировку из Pageable
        select.orderBy(criteriaBuilder.asc(fromArrangementItem.get("id")));

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
