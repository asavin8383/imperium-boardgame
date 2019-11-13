package repositories;

import enums.CheckUnitJobResult;
import model.ResultScreenShot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Created by san
 * Date: 10.11.2019
 */
public interface ResultScreenShotRepo extends JpaRepository<ResultScreenShot, Long> {

    List<ResultScreenShot> findAllByArrangementId(Long arrangementId, Pageable pageable);
    List<ResultScreenShot> findByArrangementIdAndResultIn(Long arrangementId, Collection<CheckUnitJobResult> result, Pageable pageable);

}
