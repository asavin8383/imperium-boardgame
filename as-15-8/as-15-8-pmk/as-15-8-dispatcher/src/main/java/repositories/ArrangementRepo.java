package repositories;

import model.Arrangement;
import model.enums.Reason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long> {

    @Query("select a from Arrangement a " +
            "where a.status = 'RUNNING' or a.status = 'STOPPING' and a.isManual = false")
    List<Arrangement> findReadyToUpload();

    @Query("select a from Arrangement a " +
            "where a.creationDate > :date and " +
            "(a.status = 'STOPPED' or a.status = 'STOPPING') and a.isManual = false")
    List<Arrangement> findStopped(@Param("date") LocalDateTime date);

    @Query("select a from Arrangement a " +
            "where a.status = 'RUNNING' and a.isManual = false")
    List<Arrangement> findAllRunning();

    Optional<Arrangement> findByIdAndVersion(Long id, Long version);

    @Query("select a.maxCheckUnitsCount from Arrangement a" +
            " where a.id = :arrangementId")
    Optional<Long> findMaxCheckUnitsCount(@Param("arrangementId") Long arrangementId);

    Optional<Arrangement> findByIdAndVersionAndReasonIsNot(Long id, Long version, Reason reason);
}
