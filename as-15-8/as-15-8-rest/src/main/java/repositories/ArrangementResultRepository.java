package repositories;

import model.result.ArrangementResult;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
public interface ArrangementResultRepository extends JpaRepository<ArrangementResult, Long>, ArrangementResultRepositoryAdvanced {
}
