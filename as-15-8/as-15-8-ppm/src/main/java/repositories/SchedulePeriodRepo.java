package repositories;

import model.SchedulePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by san
 * Date: 04.11.2019
 */
@Repository
public interface SchedulePeriodRepo extends JpaRepository<SchedulePeriod, Long> {
}
