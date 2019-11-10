package repositories;

import model.ResultScreenShot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by san
 * Date: 10.11.2019
 */
public interface ResultScreenShotRepo extends JpaRepository<ResultScreenShot, Long> {

    List<ResultScreenShot> findAllByArrangementId(Long arrangementId, Pageable pageable);

}
