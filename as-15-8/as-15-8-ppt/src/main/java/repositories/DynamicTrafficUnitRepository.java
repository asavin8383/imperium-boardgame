package repositories;

import model.traffic.DynamicTrafficUnit;
import model.traffic.Traffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicTrafficUnitRepository extends JpaRepository<DynamicTrafficUnit, Long> {

    @Query("select e from DynamicTrafficUnit e where e.traffic =:traffic")
    List<DynamicTrafficUnit> findByTraffic(@Param("traffic") Traffic traffic);
}
