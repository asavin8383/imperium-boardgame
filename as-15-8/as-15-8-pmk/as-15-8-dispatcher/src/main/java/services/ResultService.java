package services;

import analysis.AnalysisResult;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import jobs.ArrangementJob;
import model.Result;

import java.util.List;

public interface ResultService {

    Result saveJobResult(AnalysisResult result);

    Result updateJobStatus(Long jobID, Long erdiId, CheckUnitJobResult status, String description);

    ExecutionStatus checkArrangementStatus(Long arrangementID);

    void sendNotificationsIfFinished(Long arrangementID);
}
