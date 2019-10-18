package repositories;

import model.sor.FormalErdi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FormalErdiRepository extends JpaRepository<FormalErdi, Long>, FormalErdiRepositoryCustom {

    Page<FormalErdi> findAllByContentHistory_EndDate(LocalDateTime endDate, Pageable pageable);

    @Query(value = "select exists(select 1 from portal.erdi_traffic_units_content " +
            "where content_id = :contentId AND traffic_unit_id = :trafficUnitId)",
            nativeQuery = true)
    boolean belongsToErdiTrafficUnit(@Param("trafficUnitId") Long trafficUnitId,
                                     @Param("contentId") Long contentId);

    @Query(value = "select exists(select 1 from portal.search_query_traffic_units_content " +
            "where content_id = :contentId AND traffic_unit_id = :trafficUnitId)",
            nativeQuery = true)
    boolean belongsToSearchTrafficUnit(@Param("trafficUnitId") Long trafficUnitId,
                                       @Param("contentId") Long contentId);

}
