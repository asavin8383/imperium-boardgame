package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import repositories.ScheduleRepoAdvanced;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScheduleRepoAdvancedImpl implements ScheduleRepoAdvanced {

    private final EntityManager em;
    private CriteriaBuilder cb;
    private List<Predicate> predicates;
    Join<Schedule, SchedulePeriod> schedulePeriod;
    Join<SchedulePeriod, SchedulePeriodArrangement> spaJoin;
    private Join<SchedulePeriodArrangement, Arrangement> arrangementJoin;

    @Override
    public Page<Schedule> findAllByPlannedDateAndArrangement(LocalDate plannedDate, String query, Pageable pageable) {
        cb = em.getCriteriaBuilder();
        CriteriaQuery<Schedule> cq = cb.createQuery(Schedule.class);
        Root<Schedule> fromSchedule = cq.from(Schedule.class);
        createJoins(fromSchedule);

        predicates = new ArrayList<>();

        createQueryPredicates(predicates, query);

        if (plannedDate != null) {
            predicates.add(cb.equal(fromSchedule.get(Schedule_.PLANNED_DATE), plannedDate));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        cq.orderBy(QueryUtils.toOrders(pageable.getSort(), fromSchedule, cb));

        TypedQuery<Schedule> q = em.createQuery(cq);
        List<Schedule> res = q.getResultList();

        return new PageImpl<>(res, pageable, res.size());

    }

    private void createJoins(Root<Schedule> fromSchedule) {
        schedulePeriod = fromSchedule.join(Schedule_.SCHEDULE_PERIODS, JoinType.INNER);
        spaJoin = schedulePeriod.join(SchedulePeriod_.SCHEDULE_PERIOD_ARRANGEMENTS, JoinType.INNER);
        arrangementJoin = spaJoin.join(SchedulePeriodArrangement_.ARRANGEMENT, JoinType.INNER);
    }

    private void createQueryPredicates(List<Predicate> predicates, String query) {
        String likeQuery = "%" + query + "%";
        if (query != null) {
            predicates.add(
                cb.or(
                    cb.like(cb.lower(arrangementJoin.get(Arrangement_.ID).as(String.class)), likeQuery.toLowerCase()),
                    cb.like(cb.lower(arrangementJoin.get(Arrangement_.TITLE)), likeQuery.toLowerCase())
                )
            );
        }
    }

}
