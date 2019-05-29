package service.impl;

import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import analysis.PS_AnalysisResult;
import execution.ExecutionPSJobResult;
import service.AnalyzerService;

/**
 * Сервис проверки результата работы робота, проверяющего ПС
 * @author shabalinAI
 *
 */
@Service
public class PS_AnalyzerService implements AnalyzerService<ExecutionPSJobResult> {

	@Override
	public AnalysisResult analyzeResult(ExecutionPSJobResult result) {
		PS_AnalysisResult analysisResult = new PS_AnalysisResult();
		analysisResult.setJobID(result.getJobID());
		analysisResult.setCheckUnit(result.getCheckUnit());
		analysisResult.setCheckResult(result.isCheckResult());
		return analysisResult;
	}
}
