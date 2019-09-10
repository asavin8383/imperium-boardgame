package repositories;

import model.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Creation date: 19.08.2019
 * Author: asavin
 */
public interface ScheduleRepo extends JpaRepository<Schedule, Long> {
}
