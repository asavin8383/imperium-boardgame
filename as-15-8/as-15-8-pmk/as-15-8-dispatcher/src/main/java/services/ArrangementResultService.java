package services;

import analysis.AnalysisResult;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import jobs.ArrangementJob;
import model.ArrangementResult;

import java.util.List;

public interface ArrangementResultService {

    List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob);

    ArrangementResult processJobResult(AnalysisResult result);

    ArrangementResult updateJobStatus(Long jobID, CheckUnitJobResult status, String description);

    ExecutionStatus checkArrangementStatus(Long arrangementID);

}
