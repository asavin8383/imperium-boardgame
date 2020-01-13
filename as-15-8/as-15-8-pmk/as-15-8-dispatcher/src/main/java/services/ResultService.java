package services;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import model.Arrangement;
import model.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResultService {

    void saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult result);

    Result updateJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description);

    Page<Result> getArrangementResults(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResults,
            List<CheckUnitType> checkUnitTypes,
            String query,
            SortingDirection sortingDirection,
            String sortingColumn,
            Pageable pageable);

    long getResultsCount(Long arrangementId);

    CheckUnitResult getArrangementResult(Long arrangementId, Long resultId);
}
