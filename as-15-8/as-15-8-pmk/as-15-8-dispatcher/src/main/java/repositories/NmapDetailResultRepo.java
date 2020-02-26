package repositories;

import model.NmapDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface NmapDetailResultRepo extends JpaRepository<NmapDetailResult, Long> {

    @Query("select r from NmapDetailResult r " +
            "where r.id in :ids")
    List<NmapDetailResult> findByResultIds(@Param("ids") List<Long> resultIds);

    @Transactional
    @Modifying
    @Query(
            value = "insert into results.nmap_detail_results " +
                        "(result_id, log) " +
                    "values " +
                        "(:id, :log) " +
                    "on conflict(result_id) do update " +
                    "set " +
                        "result_id = :id, " +
                        "log = :log",
            nativeQuery = true
    )
    int upsert(
            @Param("id") Long id,
            @Param("log") String log
    );
}
