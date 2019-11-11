package repositories;

import model.traffic.ErdiTrafficUnit;
import model.traffic.ErdiTrafficUnitContent;
import model.traffic.TrafficUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErdiContentJoinRepository extends JpaRepository<ErdiTrafficUnitContent, Long> {

    List<ErdiTrafficUnitContent> findAllByTrafficUnitAndContentIdIn(ErdiTrafficUnit unit, List<Long> formalIds);

    @Query(nativeQuery = true, value = "select content_id " +
            "from portal.erdi_traffic_units_content " +
            "where traffic_unit_id = :trafficUnitId",
            countQuery = "select count(*) " +
                    "from portal.erdi_traffic_units_content " +
                    "where traffic_unit_id = :trafficUnitId")
    Page<Long> findContentIdByTrafficUnit(@Param("trafficUnitId") Long trafficUnitId, Pageable pageable);

    Page<ErdiTrafficUnitContent> findByTrafficUnit(TrafficUnit trafficUnit, Pageable pageable);

    @Query(nativeQuery = true, value =
            "select content.id " +
            "from sor.content content  " +
            "join sor.content_history history " +
                "on content.id = history.content_id " +
                    "and history.end_dt = to_date('30000101', 'YYYYMMDD') " +
            "left join portal.erdi_traffic_units_content jtable " +
                "on jtable.content_id = content.id " +
            "where not exists(select * from portal.erdi_traffic_units_content " +
                    "where content_id = content.id and traffic_unit_id = :trafficUnitId)",
            countQuery = "select count(content.id) " +
                    "from sor.content content  " +
                    "join sor.content_history history " +
                    "on content.id = history.content_id " +
                    "and history.end_dt = to_date('30000101', 'YYYYMMDD') "  +
                    "where not exists(select * from portal.erdi_traffic_units_content " +
                        "where content_id = content.id and traffic_unit_id = :trafficUnitId)")
    Page<Long> findContentIdNotInTrafficUnit(@Param("trafficUnitId") Long trafficUnitId, Pageable pageable);

}
