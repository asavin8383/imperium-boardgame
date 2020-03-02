package repositories;

import model.ResultScreenShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by san
 * Date: 10.11.2019
 */
public interface ResultScreenShotRepo extends JpaRepository<ResultScreenShot, Long> {

    @Transactional
    @Modifying
    @Query(
            value = "insert into results.result_screenshots " +
                        "(result_id, screenshot, etalon_screenshot) " +
                    "values " +
                        "(:id, :screenshot, :etalonScreenshot) " +
                    "on conflict(result_id) do update " +
                    "set " +
                        "result_id = :id, " +
                        "screenshot = :screenshot, " +
                        "etalon_screenshot = :etalonScreenshot",
            nativeQuery = true
    )
    int upsert(
            @Param("id") Long id,
            @Param("screenshot") byte[] screenshot,
            @Param("etalonScreenshot") byte[] etalonScreenshot
    );

    @Query("select r from ResultScreenShot r " +
            "where r.id in :ids")
    List<ResultScreenShot> findByResultIds(@Param("ids") List<Long> resultIds);

    @Query("select r from ResultScreenShot r " +
            "where r.id = :id")
    ResultScreenShot findByResultId(@Param("id") Long resultId);
}
