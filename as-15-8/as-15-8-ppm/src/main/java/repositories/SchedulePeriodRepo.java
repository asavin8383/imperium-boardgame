package repositories;

import model.Schedule;
import model.SchedulePeriod;
import model.enums.SchedulePeriodState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Created by san
 * Date: 04.11.2019
 */
@Repository
public interface SchedulePeriodRepo extends JpaRepository<SchedulePeriod, Long> {

    public List<SchedulePeriod> findAllByScheduleAndSchedulePeriodStateAndAndStartTimeGreaterThan(Schedule schedule, SchedulePeriodState schedulePeriodState, LocalTime time);

}
