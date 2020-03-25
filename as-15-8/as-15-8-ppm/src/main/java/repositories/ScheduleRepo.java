package repositories;

import model.Schedule;
import model.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Long>, ScheduleRepoAdvanced {

    List<Schedule> findAllByPlannedDate(LocalDate plannedDate);


    Page<Schedule> findAllByPlannedDate(LocalDate plannedDate, Pageable pageable);

    List<Schedule> findAllByPlannedDateAndStatus(LocalDate plannedDate, ScheduleStatus status);

    @Query("select min(p.startTime) from Schedule s " +
            "join s.schedulePeriods p on s.id = :schedule_id"
    )
    LocalTime getScheduleStartTime(@Param("schedule_id") Long scheduleId);

    @Query("select max(p.endTime) from Schedule s " +
            "join s.schedulePeriods p on s.id = :schedule_id"
    )
    LocalTime getScheduleEndTime(@Param("schedule_id") Long scheduleId);

    @Query("select DISTINCT s from Schedule s " +
        "join s.schedulePeriods sp " +
        "join sp.schedulePeriodArrangements spa " +
        "join spa.arrangement a on a.id = :arrangement_id"
    )
    List<Schedule> findByArrangement(@Param("arrangement_id") Long arrangementId);

    @Query("select DISTINCT max(s.id) from Schedule s " +
            "join s.schedulePeriods sp " +
            "join sp.schedulePeriodArrangements spa " +
            "join spa.arrangement a on a.id = :arrangement_id"
    )
    Long findMaxScheduleIdByArrangement(@Param("arrangement_id") Long arrangementId);

    @Query("select DISTINCT s from Schedule s " +
        "join s.schedulePeriods sp " +
        "join sp.schedulePeriodArrangements spa " +
        "join spa.arrangement a on a.id = :arrangement_id " +
        "where s.status = :status"
    )
    List<Schedule> findByStatusAndArrangement(@Param("status") ScheduleStatus status, @Param("arrangement_id") Long arrangementId);

}
