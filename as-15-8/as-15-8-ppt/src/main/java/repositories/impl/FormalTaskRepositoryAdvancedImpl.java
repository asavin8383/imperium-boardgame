package repositories.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import model.task.FormalTask;
import model.task.FormalTask_;
import repositories.FormalTaskRepositoryAdvanced;
import repositories.helpers.CriteriaHelper;

@Repository
public class FormalTaskRepositoryAdvancedImpl implements FormalTaskRepositoryAdvanced {
	
	private EntityManager em;

	@Autowired
	public FormalTaskRepositoryAdvancedImpl(EntityManager em) {
		this.em = em;
	}

	@Override
	public Page<FormalTask> findPage(Long id, String operator, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FormalTask> select = criteriaBuilder.createQuery(FormalTask.class);
		Root<FormalTask> fromFormalTask = select.from(FormalTask.class);
	 
	    List<Predicate> predicates = new ArrayList<>();
	     
	    if (id != null) {
	        predicates.add(criteriaBuilder.equal(fromFormalTask.get("id"), id));
	    }
	    if (operator != null) {
	    	predicates.add(criteriaBuilder.equal(fromFormalTask.get("operator"), operator));
	    }
	    select.where(predicates.toArray(new Predicate[0]));
	    
	    select.orderBy(QueryUtils.toOrders(pageable.getSort(), fromFormalTask, criteriaBuilder));

		return CriteriaHelper.createPage(em, select, pageable);
	    
	}

	@Override
	public List<?> getFormalTasksGroupingByStatus(){
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> select = criteriaBuilder.createQuery(Object[].class);
		Root<FormalTask> fromFormalTask = select.from(FormalTask.class);

		select.multiselect(fromFormalTask.get(FormalTask_.STATUS), criteriaBuilder.count(fromFormalTask));
		select.groupBy(fromFormalTask.get(FormalTask_.STATUS));

		return em.createQuery(select).getResultList();
	}

}
