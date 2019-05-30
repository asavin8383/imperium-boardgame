package repositories;

import checkUnits.CheckUnitType;
import model.result.ArrangementResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 */
public interface ArrangementResultRepositoryAdvanced {

    Page<ArrangementResult> findPage(Long id, Long arrangementId, String checkUnitValue, Pageable pageable, CheckUnitType checkUnitType);
}
