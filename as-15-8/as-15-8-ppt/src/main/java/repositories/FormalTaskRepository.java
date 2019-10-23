package repositories;

import enums.ExecutionStatus;
import model.task.FormalTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormalTaskRepository extends JpaRepository<FormalTask, Long>, FormalTaskRepositoryAdvanced {

	Page<FormalTask> findAllByStatus(ExecutionStatus status, Pageable pageable);
}
