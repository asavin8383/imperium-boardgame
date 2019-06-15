package repositories;

import model.result.ArrangementResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Repository
public interface ArrangementResultRepository extends JpaRepository<ArrangementResult, Long>, ArrangementResultRepositoryAdvanced {
}
