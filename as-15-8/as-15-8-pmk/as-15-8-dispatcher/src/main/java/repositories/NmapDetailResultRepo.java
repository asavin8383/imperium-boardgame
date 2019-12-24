package repositories;

import model.NmapDetailResult;
import model.ResultScreenShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NmapDetailResultRepo extends JpaRepository<NmapDetailResult, Long> {

    @Query("select r from NmapDetailResult r " +
            "where r.result in :ids")
    List<NmapDetailResult> findByResultIds(@Param("ids") List<Long> resultIds);
}
