package repositories;

import model.traffic.TrafficUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficUnitRepository extends JpaRepository<TrafficUnit, Long> {
}
