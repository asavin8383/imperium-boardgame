package repositories;

import model.enums.ExecutionStatus;
import model.task.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@Repository
public interface ArrangementRepository extends JpaRepository<Arrangement, Long>, ArrangementRepositoryAdvanced {

    List<Arrangement> findAllByStatusAndPlannedDateIsLessThan(ExecutionStatus status, LocalDateTime startDate);

    @Query("SELECT a FROM Arrangement a WHERE a.id = :id and a.status in ('NEW', 'PLANNED')")
    Optional<Arrangement> findEditableArrangement(@Param("id") Long id);
}
