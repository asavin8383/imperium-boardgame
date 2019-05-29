package services.impl;

import org.springframework.stereotype.Service;

import analysis.PS_AnalysisResult;
import enums.ArrangementUnitCheckResult;
import services.AnalysisResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
public class PS_AnalysisResultService implements AnalysisResultService<PS_AnalysisResult> {
	
	@Override
	public ArrangementUnitCheckResult processResult(PS_AnalysisResult result) {
		return result.isCheckResult() ?
				ArrangementUnitCheckResult.COMPLETED
				: ArrangementUnitCheckResult.FORBIDDEN_CONTENT_DETECTED;
	}
}
