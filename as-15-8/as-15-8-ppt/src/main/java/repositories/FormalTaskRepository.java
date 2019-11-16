package repositories;

import enums.ExecutionStatus;
import model.task.ExecutionStatusStatistics;
import model.task.FormalTask;
import model.task.Arrangement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormalTaskRepository extends JpaRepository<FormalTask, Long>, FormalTaskRepositoryAdvanced {

	Page<FormalTask> findAllByStatus(ExecutionStatus status, Pageable pageable);

	Page<FormalTask> findAllByStatusIn(List<ExecutionStatus> statuses, Pageable pageable);

	@Query("SELECT " +
			"    new model.task.ExecutionStatusStatistics(f.status, COUNT(f)) " +
			"FROM " +
			"    FormalTask f " +
			"GROUP BY " +
			"    f.status")
	List<ExecutionStatusStatistics> findSummaryByStatus();


	@Query("SELECT COUNT(f) FROM FormalTask f WHERE f.missionId=:missionId")
	Long countByMissionId(@Param("missionId") Long missionId);


	@Query("SELECT a.formalTask FROM Arrangement a WHERE a.id=:arrangementId")
	FormalTask getByArrangementId(@Param("arrangementId") Long id);
}
