package repositories;

import enums.AccessToolParameter;
import model.Robot;
import model.RobotProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by san
 * Date: 09.02.2020
 */
public interface RobotPropertyRepo extends JpaRepository<RobotProperty, Long> {

    List<RobotProperty> findByRobotAndKey(Robot robot, AccessToolParameter param);
}
