package repositories;

import model.traffic.Traffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficRepository extends JpaRepository<Traffic, Long>, TrafficRepositoryCustom {

    @Query(value = "select count(*) " +
            "from portal.erdi_traffic_units_content jointable " +
            "join portal.traffic_units units " +
                "on jointable.traffic_unit_id = units.id " +
            "where units.traffic_id = :trafficId", nativeQuery = true)
    long countContentErdiByTrafficId(@Param("trafficId") long trafficId);

    @Query(value = "select count(*) " +
            "from portal.erdi_traffic_units_custom_erdi jointable " +
            "join portal.traffic_units units " +
                "on jointable.traffic_unit_id = units.id " +
            "where units.traffic_id = :trafficId", nativeQuery = true)
    long countCustomErdiByTrafficId(@Param("trafficId") long trafficId);

    @Query(value = "select count(*) " +
            "from portal.search_query_traffic_units_search_phrases jointable " +
            "join portal.traffic_units units " +
                "on jointable.traffic_unit_id = units.id " +
            "where units.traffic_id = :trafficId and " +
                "units.name not like '%TEMPLATE'", nativeQuery = true)
    long countSearcPhrasesByTrafficId(@Param("trafficId") long trafficId);

    @Query(value = "select count(*) " +
            "from portal.traffic_units units " +
            "where units.traffic_id = :trafficId and " +
                "units.name like '%TEMPLATE';", nativeQuery = true)
    long countSearchTemplatesByTrafficId(@Param("trafficId") long trafficId);

    @Query(value = "select count(*) " +
            "from portal.traffic_units units " +
            "where units.traffic_id = :trafficId and " +
                "units.name like '%DYNAMIC';", nativeQuery = true)
    long countDynamicByTrafficId(@Param("trafficId") long trafficId);

}
