package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.traffic.Traffic;
import model.traffic.TrafficView;
import model.traffic.Traffic_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import repositories.TrafficRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficRepositoryImpl implements TrafficRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<TrafficView> findAllTrafficInfo(String name, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TrafficView> mainQuery = cb.createQuery(TrafficView.class);
        Root<Traffic> mainRoot = mainQuery.from(Traffic.class);

        // join
        // Join<Traffic, TrafficUnit> trafficUnitJoin = mainRoot.join(Traffic_.trafficUnits);

        // select
        mainQuery.multiselect(
                mainRoot.get(Traffic_.id),
                mainRoot.get(Traffic_.name)
        );
        // where
        if ( !StringUtils.isEmpty(name) ) {
            mainQuery.where(cb.like(
                    cb.upper(mainRoot.get(Traffic_.name)),
                    "%" + name.toUpperCase() + "%"));
        }
        TypedQuery<TrafficView> query = em.createQuery(mainQuery);

        CriteriaQuery<Long> countQuery = em.getCriteriaBuilder().createQuery(Long.class);
        Root<Traffic> countRoot = countQuery.from(Traffic.class);
        countQuery.select(cb.count(countRoot));
        Predicate restriction = mainQuery.getRestriction();
        if (restriction != null) {
            countQuery.where(restriction);
        }
        long total = em.createQuery(countQuery).getSingleResult();

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<TrafficView> models = query.getResultList();
        return new PageImpl<>(models, pageable, total);
    }
}
