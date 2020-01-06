package repositories;

import model.RobotSLA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RobotSLARepository extends JpaRepository<RobotSLA, Long> {

    Optional<List<RobotSLA>> findAllByRobotId(Long robotId);
}
