package repositories;

import model.traffic.CustomErdiUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiUnitRepository extends JpaRepository<CustomErdiUnit, Long> {
}
