package repositories;

import enums.ExecutionStatus;
import model.task.Arrangement;
import model.task.ArrangementStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long>, ArrangementRepositoryAdvanced {

    @Query("SELECT a FROM Arrangement a WHERE a.id = :id and a.status in ('NEW', 'FORMED')")
    Optional<Arrangement> findEditableArrangement(@Param("id") Long id);

    @Query(value = "SELECT a.accessTool FROM Arrangement a WHERE a.id = :id")
    String getAccessTool(@Param("id") Long id);

    @Query(value = "SELECT a.formalTask.missionId FROM Arrangement a WHERE a.id = :id")
    Long getMissionId(@Param("id") Long id);

    Page<Arrangement> findAllByStatus(ExecutionStatus status, Pageable pageable);

    Page<Arrangement> findAllByStatusIn(List<ExecutionStatus> statuses, Pageable pageable);

    @Query("SELECT " +
            "    new model.task.ArrangementStatistics(a.status, COUNT(a)) " +
            "FROM " +
            "    Arrangement a " +
            "GROUP BY " +
            "    a.status")
    List<ArrangementStatistics> findSummaryByStatus();

    @Query(
            "SELECT f.contentId from Arrangement a " +
                    "join a.traffic t  on a.id = :id " +
                    "join t.erdiTrafficUnits u " +
                    "join u.formalErdiList f"
    )
    List<Long> listContentIdsByArrangementId(@Param("id") Long id);
}
