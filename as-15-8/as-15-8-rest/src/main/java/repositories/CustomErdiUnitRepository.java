package repositories;

import model.traffic.CustomErdiUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiUnitRepository extends JpaRepository<CustomErdiUnit, Long> {

    Page<CustomErdiUnit> findByCustomErdiId(Long customErdiId, Pageable pageable);
}
