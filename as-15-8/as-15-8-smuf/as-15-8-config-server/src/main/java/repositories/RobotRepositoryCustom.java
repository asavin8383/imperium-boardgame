package repositories;

import model.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RobotRepositoryCustom {

    Page<Robot> findByQuery(String query, Pageable pageable);

}
