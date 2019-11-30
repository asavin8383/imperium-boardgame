package repositories;

import model.traffic.CustomErdiUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomErdiUnitRepository extends JpaRepository<CustomErdiUnit, Long> {

    Page<CustomErdiUnit> findByCustomErdiId(Long customErdiId, Pageable pageable);

    @Query ( "select units from CustomErdiUnit units " +
            " join units.customErdi erdi " +
            " join erdi.erdiTrafficUnits traffic_units " +
            "join traffic_units.traffic traffic " +
            "join Arrangement a on traffic.id = a.trafficId and a.id=:id"
    )
    List<CustomErdiUnit> findByArrangementId(@Param("id") Long arrangementId);
}
