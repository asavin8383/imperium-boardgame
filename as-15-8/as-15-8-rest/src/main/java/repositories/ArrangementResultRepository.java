package repositories;

import model.result.ArrangementResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Repository
public interface ArrangementResultRepository extends JpaRepository<ArrangementResult, Long>, ArrangementResultRepositoryAdvanced {

    List<ArrangementResult> findAllByArrangement(Long arrangementId);
}
