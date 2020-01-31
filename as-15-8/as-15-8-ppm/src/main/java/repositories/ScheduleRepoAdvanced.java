package repositories;

import model.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ScheduleRepoAdvanced  {
    Page<Schedule> findAllByPlannedDateAndArrangement(LocalDate plannedDate, String query, Pageable pageable);
}
