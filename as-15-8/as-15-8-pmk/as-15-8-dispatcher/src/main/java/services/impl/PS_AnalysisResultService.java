package services.impl;

import model.Result;
import org.springframework.stereotype.Service;

import analysis.PS_AnalysisJobResult;
import enums.CheckUnitJobResult;
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
	public void processResult(Result result, PS_AnalysisJobResult analysisResult) {

	}
}
