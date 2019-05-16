package repositories.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import model.task.FormalTask;
import repositories.FormalTaskRepositoryAdvanced;

@Repository
public class FormalTaskRepositoryAdvancedImpl implements FormalTaskRepositoryAdvanced {
	
	@Autowired
	EntityManager em;

	@Override
	public Page<FormalTask> findPage(Long taskId, Long userId, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FormalTask> select = criteriaBuilder.createQuery(FormalTask.class);
		Root<FormalTask> fromFormalTask = select.from(FormalTask.class);
	 
	    List<Predicate> predicates = new ArrayList<>();
	     
	    if (taskId != null) {
	        predicates.add(criteriaBuilder.equal(fromFormalTask.get("id"), taskId));
	    }
	    if (userId != null) {
	    	predicates.add(criteriaBuilder.equal(fromFormalTask.get("user").get("id"), userId));
	    }
	    select.where(predicates.toArray(new Predicate[0]));
	    
	    //TODO получать сортировку из Pageable
	    select.orderBy(criteriaBuilder.desc(fromFormalTask.get("creationDate")));
	    /*Order[] orders = pageable.getSort().get().toArray(size -> new Order[size]);
	    cq.orderBy(orders);*/
	    
	    TypedQuery<FormalTask> query = em.createQuery(select);
	    int totalRows = query.getResultList().size();
	    query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
	    query.setMaxResults(pageable.getPageSize());
	    
	    List<FormalTask> taskList = query.getResultList();
	    
	    return new PageImpl<FormalTask>(taskList, pageable, totalRows);
	    
	}

}
