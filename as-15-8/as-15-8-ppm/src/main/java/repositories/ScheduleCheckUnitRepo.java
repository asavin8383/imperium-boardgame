package repositories;

import model.Arrangement;
import model.ScheduleCheckUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleCheckUnitRepo extends JpaRepository<ScheduleCheckUnit, Long> {

    List<ScheduleCheckUnit> findAllByArrangement(Arrangement arrangement);

    Page<ScheduleCheckUnit> findAllByArrangement(Arrangement arrangement, Pageable pageable);

    @Query("select DISTINCT scu from ScheduleCheckUnit scu" +
            " where scu.arrangement = :ar and" +
            " (scu.checkUnitValue LIKE CONCAT('%',:query,'%') or scu.checkUnitType LIKE CONCAT('%',:query,'%') or scu.erdiId LIKE CONCAT('%',:query,'%'))")
    Page<ScheduleCheckUnit> findAllByArrangement(@Param("ar") Arrangement arrangement, @Param("query")String query, Pageable pageable);

}
