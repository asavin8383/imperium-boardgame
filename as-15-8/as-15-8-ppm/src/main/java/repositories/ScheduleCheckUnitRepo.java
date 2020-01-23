package repositories;

import checkUnits.CheckUnit;
import model.Arrangement;
import model.ScheduleCheckUnit;
import model.SchedulePeriodCheckUnit;
import model.enums.ScheduleCheckUnitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("update ScheduleCheckUnit scu set scu.status = :new_status " +
        "where scu.arrangement = :ar and scu.status = :current_status")
    void changeStatus(@Param("ar") Arrangement arrangement, @Param("current_status") ScheduleCheckUnitStatus currentStatus, @Param("new_status")ScheduleCheckUnitStatus newStatus);

    @Modifying
    @Query("update ScheduleCheckUnit scu set scu.status = :new_status " +
        "where scu.arrangement = :ar and scu in (:check_units)")
    void changeStatus(@Param("ar") Arrangement arrangement, @Param("check_units") List<CheckUnit> checkUnits, @Param("new_status")ScheduleCheckUnitStatus newStatus);

    @Modifying
    @Query("update ScheduleCheckUnit scu set scu.status = :new_status " +
        "where scu.id in (:schedule_period_check_units) and scu.status = :current_status")
    void changeStatus(@Param("schedule_period_check_units") List<Long> schedulePeriodCheckUnits, @Param("current_status") ScheduleCheckUnitStatus currentStatus, @Param("new_status")ScheduleCheckUnitStatus newStatus);
}
