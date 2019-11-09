package repositories;

import enums.ExecutionStatus;
import model.task.ArrangementStatistics;
import model.task.FormalTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormalTaskRepository extends JpaRepository<FormalTask, Long>, FormalTaskRepositoryAdvanced {

	Page<FormalTask> findAllByStatus(ExecutionStatus status, Pageable pageable);

	Page<FormalTask> findAllByStatusIn(List<ExecutionStatus> statuses, Pageable pageable);

	@Query("SELECT " +
			"    new model.task.ArrangementStatistics(f.status, COUNT(f)) " +
			"FROM " +
			"    FormalTask f " +
			"GROUP BY " +
			"    f.status")
	List<ArrangementStatistics> findSummaryByStatus();
}
