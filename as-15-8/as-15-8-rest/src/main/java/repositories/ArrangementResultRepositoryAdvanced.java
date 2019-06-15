package repositories;

import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import model.result.ArrangementResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 */
public interface ArrangementResultRepositoryAdvanced {

    Page<ArrangementResult> findPage(Long id, Long arrangementId, String checkUnitValue, Pageable pageable, CheckUnitType checkUnitType, List<CheckUnitJobResult> checkUnitJobResults);
}
