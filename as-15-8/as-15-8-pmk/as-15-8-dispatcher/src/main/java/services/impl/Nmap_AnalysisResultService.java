package services.impl;

import analysis.NMapAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.NmapDetailResult;
import model.PasdDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.NmapDetailResultRepo;
import repositories.PasdDetailResultRepo;
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

	private final NmapDetailResultRepo nmapDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.NMAP;
	}

	@Override
	public void saveResult(Result result, NMapAnalysisJobResult analysisResult) {
		NmapDetailResult nmapDetailResult = new NmapDetailResult();
		nmapDetailResult.setResult(result);
		nmapDetailResult.setLog(analysisResult.getNmapLog());
		nmapDetailResultRepo.save(nmapDetailResult);
	}
}
