package services.impl;

import analysis.NMapAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.NmapDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.AnalysisResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NmapAnalysisResultService implements AnalysisResultService<NMapAnalysisJobResult> {

	@Override
	public CheckType getCheckType() {
		return CheckType.NMAP;
	}

	@Override
	public DetailResult createDetails(Result result, NMapAnalysisJobResult analysisResult) {
		NmapDetailResult nmapDetailResult = new NmapDetailResult();

		nmapDetailResult.setResult(result);

		nmapDetailResult.setLog(analysisResult.getNmapLog());
		return nmapDetailResult;
	}

	@Override
	public String getErrorText(NMapAnalysisJobResult analysisResult) {
		return analysisResult.getNmapLog();
	}
}
