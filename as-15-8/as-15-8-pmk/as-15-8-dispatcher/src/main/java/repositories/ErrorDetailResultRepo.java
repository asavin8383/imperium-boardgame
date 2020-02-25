package repositories;

import model.ErrorDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface ErrorDetailResultRepo extends JpaRepository<ErrorDetailResult, Long> {

    @Transactional
    @Modifying
    @Query(
            value = "insert into results.error_detail_results " +
                        "(result_id, error) " +
                    "values " +
                        "(:id, :error) " +
                    "on conflict do update " +
                    "set " +
                        "result_id = :id, " +
                        "error = :error",
            nativeQuery = true
    )
    int upsert(
            @Param("id") Long id,
            @Param("error") String error
    );
}
