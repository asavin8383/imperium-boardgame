package repositories;

import model.Arrangement;
import model.SchedulePeriodArrangement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Creation date: 23.08.2019
 * Author: asavin
 */
public interface SchedulePeriodArrangementRepo extends JpaRepository<SchedulePeriodArrangement, Long> {

    List<SchedulePeriodArrangement> findAllByArrangement(Arrangement arrangement);
}
