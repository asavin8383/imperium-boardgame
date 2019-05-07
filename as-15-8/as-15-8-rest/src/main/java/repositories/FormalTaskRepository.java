package repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import model.task.FormalTask;

public interface FormalTaskRepository extends JpaRepository<FormalTask, Long> {

	Page<FormalTask> findByAuthor_Id(Long userId, Pageable pageable);
}
