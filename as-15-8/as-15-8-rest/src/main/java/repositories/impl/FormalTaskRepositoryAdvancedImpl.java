package repositories.impl;

import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public Page<FormalTask> findPage(Long id, Long userId, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FormalTask> select = criteriaBuilder.createQuery(FormalTask.class);
		Root<FormalTask> fromFormalTask = select.from(FormalTask.class);
	 
	    List<Predicate> predicates = new ArrayList<>();
	     
	    if (id != null) {
	        predicates.add(criteriaBuilder.equal(fromFormalTask.get("id"), id));
	    }
	    if (userId != null) {
	    	predicates.add(criteriaBuilder.equal(fromFormalTask.get("user").get("id"), userId));
	    }
	    select.where(predicates.toArray(new Predicate[0]));
	    
	    //TODO получать сортировку из Pageable
	    select.orderBy(criteriaBuilder.desc(fromFormalTask.get("creationDate")));
	    /*Order[] orders = pageable.getSort().get().toArray(size -> new Order[size]);
	    cq.orderBy(orders);*/

		return CriteriaHelper.createPage(em, select, pageable);
	    
	}

}
