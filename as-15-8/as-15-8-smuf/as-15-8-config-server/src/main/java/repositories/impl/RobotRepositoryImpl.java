package repositories.impl;

import helpers.CriteriaHelper;
import lombok.RequiredArgsConstructor;
import model.Robot;
import model.Robot_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.StringUtils;
import repositories.RobotRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class RobotRepositoryImpl implements RobotRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Robot> findByQuery(String query, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Robot> mainQuery = cb.createQuery(Robot.class);
        Root<Robot> mainRoot = mainQuery.from(Robot.class);
        List<Predicate> predicates = new ArrayList<>();

        List<SingularAttribute<Robot, String>> likeAttrs = new ArrayList<>();
        likeAttrs.add(Robot_.name);

        if (!StringUtils.isEmpty(query)) {
            String likeQuery = "%" + query.toLowerCase() + "%";
            for (SingularAttribute<Robot, String> attr : likeAttrs) {
                predicates.add(cb.like(cb.lower(mainRoot.get(attr)), likeQuery));
            }
            Predicate predicate = cb.or(predicates.toArray(new Predicate[0]));
            mainQuery.where(predicate);
        }

        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));
        return CriteriaHelper.createPage(em, mainQuery, pageable);
    }
}