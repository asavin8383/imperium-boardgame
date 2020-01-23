package repositories;

import model.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long> {

    @Query("select a from Arrangement a " +
            "where a.status = 'RUNNING' or a.status = 'STOPPED'")
    List<Arrangement> findReadyToUpload();

    @Query("select a from Arrangement a " +
            "where a.creationDate = :date and " +
            "a.status = 'STOPPED'")
    List<Arrangement> findStopped(@Param("date") LocalDateTime date);
}
