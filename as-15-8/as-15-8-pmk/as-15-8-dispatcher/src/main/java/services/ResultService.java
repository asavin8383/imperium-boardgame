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
}
