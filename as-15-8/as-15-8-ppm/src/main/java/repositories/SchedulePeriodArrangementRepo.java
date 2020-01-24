package repositories;

import model.Arrangement;
import model.SchedulePeriod;
import model.SchedulePeriodArrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Creation date: 23.08.2019
 * Author: asavin
 */
public interface SchedulePeriodArrangementRepo extends JpaRepository<SchedulePeriodArrangement, Long> {

    List<SchedulePeriodArrangement> findAllByArrangement(Arrangement arrangement);

    List<SchedulePeriodArrangement> findAllBySchedulePeriod(SchedulePeriod schedulePeriod);

    @Query(
        "select spa from SchedulePeriodArrangement spa " +
        "join spa.arrangement a on a.id = :arrangement_id " +
        "join spa.schedulePeriod sp " +
        "join sp.schedule s on s.id = :schedule_id"
    )
    List<SchedulePeriodArrangement> findAllByScheduleAndArrangement(@Param("schedule_id") Long scheduleId, @Param("arrangement_id") Long arrangementId);

}
