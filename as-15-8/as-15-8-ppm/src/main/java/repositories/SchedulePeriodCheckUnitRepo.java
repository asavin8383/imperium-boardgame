package repositories;

import model.SchedulePeriod;
import model.SchedulePeriodArrangement;
import model.SchedulePeriodCheckUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Creation date: 23.08.2019
 * Author: asavin
 */
public interface SchedulePeriodCheckUnitRepo extends JpaRepository<SchedulePeriodCheckUnit, Long> {
    List<SchedulePeriodCheckUnit> findAllBySchedulePeriodArrangement(SchedulePeriodArrangement schedulePeriodArrangement);

    @Query("select cu.executionNumber from SchedulePeriodCheckUnit cu " +
            "join cu.schedulePeriodArrangement a " +
            "join a.schedulePeriod p " +
            "on ((p.startTime < :startTime and p.endTime > :startTime) or " +
            "(p.startTime > :startTime and p.startTime > :endTime)) " +
            "join p.schedule s " +
            "on s.status in ('PLANNED', 'RUNNING') and " +
            "s.plannedDate = :plannedDate"
    )
    List<Long> getBusyExecutionNumbers(@Param("plannedDate") LocalDate plannedDate, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    @Query("select COUNT(spcu) from SchedulePeriodCheckUnit spcu " +
        "join spcu.schedulePeriodArrangement spa " +
        "join spa.arrangement a on a.id = :arrangement_id"
    )
    Long getSchedulePeriodCheckUnitCount(@Param("arrangement_id") Long arrangementId);
}
