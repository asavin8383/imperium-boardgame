package repositories;

import model.Schedule;
import model.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByPlannedDate(LocalDate plannedDate);

    List<Schedule> findAllByPlannedDateAndStatus(LocalDate plannedDate, ScheduleStatus status);

    @Query("select sum(s.maxWorkersCount) from Schedule s " +
            "where s.status = 'RUNNING' and " +
            "s.plannedDate = :plannedDate " +
            "group by s.id")
    int getBusyWorkersCount(@Param("plannedDate") LocalDate plannedDate);
}
