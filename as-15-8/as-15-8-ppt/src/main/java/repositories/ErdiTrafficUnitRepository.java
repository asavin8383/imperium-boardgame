package repositories;

import model.traffic.ErdiTrafficUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ErdiTrafficUnitRepository extends JpaRepository<ErdiTrafficUnit, Long> {

    Optional<ErdiTrafficUnit> findFirstByNameEndingWithIgnoreCaseOrderByIdDesc(String suffix);
}
