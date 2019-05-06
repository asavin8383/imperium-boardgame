package repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import model.task.FormalTask;

public interface FormalTaskRepository extends JpaRepository<FormalTask, Long> {

}
