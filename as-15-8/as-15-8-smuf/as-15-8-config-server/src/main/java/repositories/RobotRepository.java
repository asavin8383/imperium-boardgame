package repositories;

import model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long>
{
    List<Robot> findByOrigName(String name);

    List<Robot> findByName(String name);
}
