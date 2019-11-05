package repositories;

import model.SchedulePeriod;
import model.SchedulePeriodArrangement;
import model.SchedulePeriodCheckUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Creation date: 23.08.2019
 * Author: asavin
 */
public interface SchedulePeriodCheckUnitRepo extends JpaRepository<SchedulePeriodCheckUnit, Long> {
    List<SchedulePeriodCheckUnit> findAllBySchedulePeriodArrangement(SchedulePeriodArrangement schedulePeriodArrangement);
}
