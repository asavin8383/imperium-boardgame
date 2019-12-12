package repositories;

import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import model.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ResultRepoAdvanced {

    Page<Result> findByFilter(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResultNames,
            List<CheckUnitType> checkUnitTypes,
            String query,
            Pageable pageable
    );
}
