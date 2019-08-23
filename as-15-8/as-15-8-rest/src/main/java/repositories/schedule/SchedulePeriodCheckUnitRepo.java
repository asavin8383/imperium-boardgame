package repositories.schedule;

import model.schedule.SchedulePeriodCheckUnit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Creation date: 23.08.2019
 * Author: asavin
 */
public interface SchedulePeriodCheckUnitRepo extends JpaRepository<SchedulePeriodCheckUnit, Long> {
}
