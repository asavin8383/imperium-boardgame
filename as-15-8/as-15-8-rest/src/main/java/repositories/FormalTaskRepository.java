package repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import model.task.FormalTask;
import org.springframework.stereotype.Repository;

@Repository
public interface FormalTaskRepository extends JpaRepository<FormalTask, Long>, FormalTaskRepositoryAdvanced {

	Page<FormalTask> findByUser_Id(Long userId, Pageable pageable);
}
