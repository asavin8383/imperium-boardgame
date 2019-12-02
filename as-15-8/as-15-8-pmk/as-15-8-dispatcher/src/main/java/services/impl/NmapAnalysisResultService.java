package services.impl;

import analysis.NMapAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.NmapDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.NmapDetailResultRepo;
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

	private final NmapDetailResultRepo nmapDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.NMAP;
	}

	@Override
	public void saveResult(Result result, NMapAnalysisJobResult analysisResult) {
		NmapDetailResult nmapDetailResult = nmapDetailResultRepo.findById(result.getId())
				.orElseGet(NmapDetailResult::new);
		BeanUtils.copyProperties(nmapDetailResult, new NmapDetailResult(), "id", "result");

		nmapDetailResult.setResult(result);

		nmapDetailResult.setLog(analysisResult.getNmapLog());
		nmapDetailResultRepo.save(nmapDetailResult);
	}

	@Override
	public String getErrorText(NMapAnalysisJobResult analysisResult) {
		return analysisResult.getNmapLog();
	}
}
