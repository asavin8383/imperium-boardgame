package repositories;

import model.Schedule;
import model.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Long> {

    Page<Schedule> findAllByPlannedDate(LocalDate plannedDate, Pageable pageable);

    List<Schedule> findAllByPlannedDateAndStatus(LocalDate plannedDate, ScheduleStatus status);
}
