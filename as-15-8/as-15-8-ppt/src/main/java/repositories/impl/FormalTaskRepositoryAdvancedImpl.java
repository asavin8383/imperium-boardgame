package repositories.impl;

import model.enums.ExecutionStatus;
import model.task.FormalTask;
import model.task.FormalTask_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import repositories.FormalTaskRepositoryAdvanced;
import repositories.helpers.CriteriaHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FormalTaskRepositoryAdvancedImpl implements FormalTaskRepositoryAdvanced {
	
	private EntityManager em;

	@Autowired
	public FormalTaskRepositoryAdvancedImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public Page<FormalTask> findPage(List<ExecutionStatus> statuses, Long id, String operator, String fgisId, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FormalTask> select = criteriaBuilder.createQuery(FormalTask.class);
		Root<FormalTask> fromFormalTask = select.from(FormalTask.class);
	 
	    List<Predicate> predicates = new ArrayList<>();
	     
	    if (id != null) {
	        predicates.add(criteriaBuilder.equal(fromFormalTask.get("id"), id));
	    }
	    if (operator != null) {
	    	predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromFormalTask.get(FormalTask_.OPERATOR)), "%" + operator.toUpperCase() + "%"));
	    }
	    if (fgisId != null) {
	    	predicates.add(criteriaBuilder.like(criteriaBuilder.upper(fromFormalTask.get(FormalTask_.FGIS_ID)), "%" + fgisId.toUpperCase() + "%"));
		}

	    if (statuses != null &&  statuses.size()>0) {
	    	predicates.add(fromFormalTask.get(FormalTask_.STATUS).in(statuses));
		}

	    select.where(predicates.toArray(new Predicate[0]));
	    
	    select.orderBy(QueryUtils.toOrders(pageable.getSort(), fromFormalTask, criteriaBuilder));

		return CriteriaHelper.createPage(em, select, pageable);
	    
	}

}
