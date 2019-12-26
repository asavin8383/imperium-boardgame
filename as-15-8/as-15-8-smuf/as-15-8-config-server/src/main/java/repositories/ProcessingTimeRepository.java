package repositories;

import model.ProcessingTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessingTimeRepository extends JpaRepository<ProcessingTime, Long> {

    Optional<List<ProcessingTime>> findAllByRobotId(Long id);
}
