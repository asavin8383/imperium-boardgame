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

    List<ScheduleCheckUnit> findAllByArrangementAndFinished(Arrangement arrangement, boolean finished);

    Page<ScheduleCheckUnit> findAllByArrangement(Arrangement arrangement, Pageable pageable);

    @Query("select DISTINCT scu from ScheduleCheckUnit scu" +
            " where scu.arrangement = :ar and" +
            " (scu.checkUnitValue LIKE CONCAT('%',:query,'%') or scu.checkUnitType LIKE CONCAT('%',:query,'%') or scu.erdiId LIKE CONCAT('%',:query,'%'))")
    Page<ScheduleCheckUnit> findAllByArrangement(@Param("ar") Arrangement arrangement, @Param("query")String query, Pageable pageable);

    @Modifying
    @Query("update ScheduleCheckUnit scu set scu.finished = :finished " +
        "where scu.arrangement = :arrangement and scu.id in (:schedule_check_units)")
    void changeFinished(@Param("arrangement") Arrangement arrangement,
                        @Param("schedule_check_units") List<Long> scheduleCheckUnits,
                        @Param("finished")boolean finished);

    @Query("select count(scu) from ScheduleCheckUnit scu" +
            " where scu.arrangement = :ar")
    Long findCheckUnitsCount(@Param("ar") Arrangement arrangement);
}
