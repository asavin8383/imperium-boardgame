package repositories;

import model.Robot;
import model.RobotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long>, RobotRepositoryCustom
{
    List<Robot> findAllByType(RobotType type);

    List<Robot> findByOrigName(String name);

    List<Robot> findByName(String name);

    @Transactional
    void deleteByOrigIdAndType(Long origId, RobotType type);
}
