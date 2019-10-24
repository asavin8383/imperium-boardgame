package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.Mission;
import model.scheme.Mission_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.StringUtils;
import repositories.MissionRepositoryCustom;
import repositories.helper.CriteriaHelper;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Mission> findByQuery(String query, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Mission> mainQuery = cb.createQuery(Mission.class);
        Root<Mission> mainRoot = mainQuery.from(Mission.class);
        List<Predicate> predicates = new ArrayList<>();

        List<SingularAttribute<Mission, String>> likeAttrs = new ArrayList<>();
        likeAttrs.add(Mission_.origId);
        likeAttrs.add(Mission_.docNum);

        if (!StringUtils.isEmpty(query)) {
            String likeQuery = "%" + query.toLowerCase() + "%";
            for (SingularAttribute<Mission, String> attr : likeAttrs) {
                predicates.add(cb.like(cb.lower(mainRoot.get(attr)), likeQuery));
            }
            Predicate predicate = cb.or(predicates.toArray(new Predicate[0]));
            mainQuery.where(predicate);
        }

        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));
        return CriteriaHelper.createPage(em, mainQuery, pageable);
    }
}