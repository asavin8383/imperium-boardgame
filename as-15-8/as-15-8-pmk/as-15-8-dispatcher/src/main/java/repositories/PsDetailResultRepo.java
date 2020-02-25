package repositories;

import model.PsDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface PsDetailResultRepo extends JpaRepository<PsDetailResult, Long> {

    @Modifying
    @Query(
            value = "insert into results.ps_detail_results " +
                        "(result_id, description) " +
                    "values " +
                        "(:id, :description) " +
                    "on conflict do update " +
                    "set " +
                        "result_id = :id, " +
                        "description = :description",
            nativeQuery = true
    )
    int upsert(
            @Param("id") Long id,
            @Param("description") String description
    );
}
