package services.impl;

import analysis.NMapAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.DetailResultRepo;
import services.AnalysisResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class Nmap_AnalysisResultService implements AnalysisResultService<NMapAnalysisJobResult> {

	private final DetailResultRepo detailResultRepo;

	@Override
	public void processResult(Result result, NMapAnalysisJobResult analysisResult) {
		DetailResult detailResult = new DetailResult();

		detailResult.setResult(result);

		detailResult.setStubScoreInfo(analysisResult.getNmapLog());

		detailResultRepo.save(detailResult);
	}
}
