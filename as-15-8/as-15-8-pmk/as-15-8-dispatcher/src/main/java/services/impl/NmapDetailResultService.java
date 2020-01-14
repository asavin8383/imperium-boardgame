package services.impl;

import analysis.NMapAnalysisJobResult;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.NmapDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.NmapDetailResultRepo;
import services.DetailResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NmapDetailResultService implements DetailResultService<NMapAnalysisJobResult> {

	private final NmapDetailResultRepo nmapDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.NMAP;
	}

	@Override
	public DetailResult create(Result result, NMapAnalysisJobResult analysisResult) {
		NmapDetailResult nmapDetailResult = nmapDetailResultRepo.findById(result.getJobId()).orElse(new NmapDetailResult());

		nmapDetailResult.setResult(result);

		nmapDetailResult.setLog(analysisResult.getNmapLog());
		return nmapDetailResult;
	}

	@Override
	public void save(DetailResult detailResult) {
		if(!(detailResult instanceof NmapDetailResult))
			throw new AS_15_8_DispatcherException("Ошибка при сохранении детального результата типа " + detailResult.getClass().getSimpleName());
		nmapDetailResultRepo.save((NmapDetailResult) detailResult);
	}

	@Override
	public String getErrorText(NMapAnalysisJobResult analysisResult) {
		return analysisResult.getNmapLog();
	}
}
