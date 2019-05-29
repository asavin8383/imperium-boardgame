package service.impl;

import static enums.ArrangementUnitCheckResult.CAPTCHA_DETECTED;
import static enums.ArrangementUnitCheckResult.COMPLETED;
import static enums.ArrangementUnitCheckResult.FORBIDDEN_CONTENT_DETECTED;

import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import analysis.PS_AnalysisJobResult;
import enums.ArrangementUnitCheckResult;
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
		PS_AnalysisJobResult analysisResult = new PS_AnalysisJobResult();
		analysisResult.setJobID(result.getJobID());
		analysisResult.setCheckUnit(result.getCheckUnit());
		analysisResult.setCheckResult(obtainResult(result));
		analysisResult.setScreenshot(result.getScreenshot());
		return analysisResult;
	}

	private ArrangementUnitCheckResult obtainResult(ExecutionPSJobResult result) {
		return result.isCaptchaDetected() ? CAPTCHA_DETECTED :
				(result.isLinkFound() ? FORBIDDEN_CONTENT_DETECTED : COMPLETED);
	}
}
