package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiRepository extends JpaRepository<CustomErdi, Long>, CustomErdiRepositoryCustom {

    @Query(value = "select exists(select 1 from portal.erdi_traffic_units_custom_erdi " +
            "where custom_erdi_id = :customId AND traffic_unit_id = :trafficUnitId)",
            nativeQuery = true)
    boolean belongsToErdiTrafficUnit(@Param("trafficUnitId") Long trafficUnitId,
                                     @Param("customId") Long customId);

    @Query(value = "select exists(select 1 from portal.search_query_traffic_units_custom_erdi " +
            "where content_id = :customId AND traffic_unit_id = :trafficUnitId)",
            nativeQuery = true)
    boolean belongsToSearchTrafficUnit(@Param("trafficUnitId") Long trafficUnitId,
                                       @Param("customId") Long customId);

}
