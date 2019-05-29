package services.impl;

import org.springframework.stereotype.Service;

import analysis.PS_AnalysisJobResult;
import enums.ArrangementUnitCheckResult;
import services.AnalysisResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
public class PS_AnalysisResultService implements AnalysisResultService<PS_AnalysisJobResult> {
	
	@Override
	public ArrangementUnitCheckResult processResult(PS_AnalysisJobResult result) {
		return result.getCheckResult();
	}
}
