package repositories;

import model.ResultScreenShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by san
 * Date: 10.11.2019
 */
public interface ResultScreenShotRepo extends JpaRepository<ResultScreenShot, Long> {

    @Query("select r from ResultScreenShot r " +
            "where r.id in :ids")
    List<ResultScreenShot> findByResultIds(@Param("ids") List<Long> resultIds);

    @Query("select r from ResultScreenShot r " +
            "where r.id = :id")
    ResultScreenShot findByResultId(@Param("id") Long resultId);
}
