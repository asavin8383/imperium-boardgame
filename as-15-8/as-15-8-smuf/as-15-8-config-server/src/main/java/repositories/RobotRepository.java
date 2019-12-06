package repositories;

import model.Robot;
import model.RobotStatus;
import model.RobotType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {

    Page<Robot> findPage(Pageable pageable);

    List<Robot> findAllByType(RobotType type);

    List<Robot> findAllByStatus(RobotStatus status);

    List<Robot> findByName(String name);

    @Transactional
    void deleteByOrigIdAndType(Long origId, RobotType type);
}
