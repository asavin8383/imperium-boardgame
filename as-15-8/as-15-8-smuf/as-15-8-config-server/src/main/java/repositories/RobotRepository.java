package repositories;

import model.Robot;
import model.RobotStatus;
import model.RobotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {

    List<Robot> findAllByType(RobotType type);

    List<Robot> findAllByStatus(RobotStatus status);

    List<Robot> findByName(String name);

    Optional<Robot> findByTypeAndOrigId(RobotType type, Long origId);
}
