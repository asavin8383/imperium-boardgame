package repositories;

import model.Schedule;
import model.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByPlannedDate(LocalDate plannedDate);

    List<Schedule> findAllByPlannedDateAndStatus(LocalDate plannedDate, ScheduleStatus status);

    @Query("select coalesce(sum(s.maxWorkersCount), 0) from Schedule s " +
            "join s.schedulePeriods p on " +
            "(p.startTime < :startTime and p.endTime > :startTime) or (p.startTime > :startTime and p.startTime > :endTime) " +
            "and s.status in ('PLANNED', 'RUNNING') and " +
            "s.plannedDate = :plannedDate"
    )
    int getBusyWorkersCount(@Param("plannedDate") LocalDate plannedDate, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    @Query("select min(p.startTime) from Schedule s " +
            "join s.schedulePeriods p on s.id = :schedule_id"
    )
    LocalTime getScheduleStartTime(@Param("schedule_id") Long scheduleId);

    @Query("select max(p.endTime) from Schedule s " +
            "join s.schedulePeriods p on s.id = :schedule_id"
    )
    LocalTime getScheduleEndTime(@Param("schedule_id") Long scheduleId);
}
