package services;

import analysis.AnalysisResult;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import jobs.ArrangementJob;
import model.Result;

import java.util.List;

public interface ArrangementResultService {

    Result processJobResult(AnalysisResult result);

    Result updateJobStatus(Long jobID, CheckUnitJobResult status, String description);

    ExecutionStatus checkArrangementStatus(Long arrangementID);

}
