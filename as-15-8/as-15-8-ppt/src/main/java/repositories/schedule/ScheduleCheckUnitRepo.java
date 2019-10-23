package repositories.schedule;

import model.schedule.ScheduleCheckUnit;
import model.task.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleCheckUnitRepo extends JpaRepository<ScheduleCheckUnit, Long> {

    List<ScheduleCheckUnit> findAllByArrangement(Arrangement arrangement);
}
