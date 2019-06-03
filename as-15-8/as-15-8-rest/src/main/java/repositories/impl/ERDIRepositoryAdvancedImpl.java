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
import org.springframework.data.jpa.repository.query.QueryUtils;
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

    private static final String URL = "URL";

    private EntityManager em;

    @Autowired
    public ERDIRepositoryAdvancedImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<ERDI> findPage(Long id, Long arrangementId, String organization, String blocktype, Pageable pageable) {
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
        //TODO Безжалостный хардкод в таблице URL - это NULL
        if(blocktype != null && !blocktype.toUpperCase().equals(URL)){
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(erdi.get(ERDI_.BLOCKTYPE)), "%" + blocktype.toLowerCase() + "%" ));
        }
        if(blocktype != null && blocktype.toUpperCase().equals(URL)){
            predicates.add(criteriaBuilder.isNull(criteriaBuilder.lower(erdi.get(ERDI_.BLOCKTYPE))));
        }

        select.where(predicates.toArray(new Predicate[0]));

        select.orderBy(QueryUtils.toOrders(pageable.getSort(), erdi, criteriaBuilder));

        return CriteriaHelper.createPage(em, select, pageable);
    }
}
