package repositories;

import enums.CheckUnitJobResult;
import model.ResultScreenShot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Created by san
 * Date: 10.11.2019
 */
public interface ResultScreenShotRepo extends JpaRepository<ResultScreenShot, Long> {


    @Query("select rs from ResultScreenShot rs " +
            "join Result r on r.arrangementId = :arrangementId and r.result in (:result)")
    List<ResultScreenShot> findByArrangementIdAndResultIn(@Param("arrangementId") Long arrangementId, @Param("result") Collection<CheckUnitJobResult> result, Pageable pageable);

}
