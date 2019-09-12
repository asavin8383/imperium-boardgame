package repositories;

import model.traffic.CustomErdiUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomErdiUnitRepository extends JpaRepository<CustomErdiUnit, Long> {

    List<CustomErdiUnit> findByCustomErdiIdOrderById(Long customErdiId);
}
