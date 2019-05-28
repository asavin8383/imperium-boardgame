package service.impl;

import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import analysis.PS_AnalysisJobResult;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import service.AnalyzerService;

/**
 * Сервис проверки результата работы робота, проверяющего ПС
 * @author shabalinAI
 *
 */
@Service
public class PS_AnalyzerService implements AnalyzerService {
	
	@Override
	public Class<? extends ExecutionJobResult> getExecutionResultType() {
		return ExecutionPSJobResult.class;
	}

	@Override
	public AnalysisResult analyzeResult(ExecutionJobResult result) {
		PS_AnalysisJobResult analysisResult = new PS_AnalysisJobResult();
		analysisResult.setJobID(result.getJobID());
		analysisResult.setCheckUnit(result.getCheckUnit());
		analysisResult.setCheckResult(((ExecutionPSJobResult)result).isCheckResult());
		return analysisResult;
	}
}
