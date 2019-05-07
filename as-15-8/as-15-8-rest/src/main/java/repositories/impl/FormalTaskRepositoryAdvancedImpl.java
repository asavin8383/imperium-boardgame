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
		CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery<FormalTask> cq = cb.createQuery(FormalTask.class);
	 
	    Root<FormalTask> formalTask = cq.from(FormalTask.class);
	    List<Predicate> predicates = new ArrayList<>();
	     
	    if (taskId != null) {
	        predicates.add(cb.equal(formalTask.get("id"), taskId));
	    }
	    if (userId != null) {
	        predicates.add(cb.equal(formalTask.get("user.id"), userId));
	    }
	    cq.where(predicates.toArray(new Predicate[0]));
	    
	    //TODO получать сортировку из Pageable
	    cq.orderBy(cb.desc(formalTask.get("creationDate")));
	    
	    TypedQuery<FormalTask> query = em.createQuery(cq);
	    int totalRows = query.getResultList().size();
	    query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
	    query.setMaxResults(pageable.getPageSize());
	    
	    List<FormalTask> taskList = query.getResultList();
	    
	    return new PageImpl<FormalTask>(taskList, pageable, totalRows);
	    
	}

}
