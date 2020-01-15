package services;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import jobs.ArrangementJob;
import model.Arrangement;
import model.Result;

import java.util.List;

public interface ResultService {

    Result saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult result);

    Result updateJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description);

    ExecutionStatus checkArrangementStatus(Long arrangementID);

    void sendNotificationsIfFinished(Long arrangementID);

    ExecutionStatus getArrangementExecutionStatus(Long arrangementID);

    int getArrangementsCount(Long id);

}
