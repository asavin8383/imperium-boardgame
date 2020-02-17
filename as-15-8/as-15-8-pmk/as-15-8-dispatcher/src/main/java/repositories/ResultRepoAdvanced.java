package repositories;

import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import model.Result;
import model.enums.UserResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ResultRepoAdvanced {

    Page<Result> findByFilter(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResultNames,
            List<CheckUnitType> checkUnitTypes,
            List<UserResult> userResults,
            String query,
            Pageable pageable
    );
}
