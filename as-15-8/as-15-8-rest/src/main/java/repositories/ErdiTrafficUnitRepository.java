package repositories;

import model.traffic.ErdiTrafficUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErdiTrafficUnitRepository extends JpaRepository<ErdiTrafficUnit, Long> {
}
