package repositories;

import model.result.ArrangementResult;
import model.task.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Repository
public interface ArrangementResultRepo extends JpaRepository<ArrangementResult, Long>, ArrangementResultRepositoryAdvanced {

    List<ArrangementResult> findAllByArrangement(Arrangement arrangement);
}
