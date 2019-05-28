package service.impl;

import analysis.AnalysisResult;
import analysis.PS_AnalysisJobResult;
import enums.ArrangementUnitCheckResult;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import org.springframework.stereotype.Service;
import service.AnalyzerService;

import static enums.ArrangementUnitCheckResult.*;

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
		analysisResult.setCheckResult(obtainResult(result));
		return analysisResult;
	}

	private ArrangementUnitCheckResult obtainResult(ExecutionJobResult result) {
		ExecutionPSJobResult psResult = (ExecutionPSJobResult) result;
		return psResult.isCaptchaDetected() ? CAPTCHA_DETECTED :
				(psResult.isLinkFound() ? FORBIDDEN_CONTENT_DETECTED : COMPLETED);
	}
}
