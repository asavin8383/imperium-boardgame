package services.impl;

import analysis.NMapAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.NmapDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.NmapDetailResultRepo;
import services.DetailResultService;

import javax.persistence.EntityManager;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NmapDetailResultService implements DetailResultService<NMapAnalysisJobResult, NmapDetailResult> {

	private final NmapDetailResultRepo nmapDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.NMAP;
	}

	@Override
	public NmapDetailResult create(NMapAnalysisJobResult nmapAnalysisResult) {
		NmapDetailResult nmapDetailResult = new NmapDetailResult();
		fill(nmapDetailResult, nmapAnalysisResult);
		return nmapDetailResult;
	}

	@Override
	public NmapDetailResult getOrCreate(Result result, NMapAnalysisJobResult nmapAnalysisResult) {
		NmapDetailResult nmapDetailResult = nmapDetailResultRepo.findById(result.getId()).orElseGet(NmapDetailResult::new);
		nmapDetailResult.setResult(result);
		fill(nmapDetailResult, nmapAnalysisResult);
		return nmapDetailResult;
	}

	private void fill(NmapDetailResult nmapDetailResult, NMapAnalysisJobResult nmapAnalysisResult) {
		nmapDetailResult.setLog(nmapAnalysisResult.getNmapLog());
	}

	@Override
	public void save(EntityManager entityManager, DetailResult nmapDetailResult) {
		NmapDetailResult detailResult = (NmapDetailResult) nmapDetailResult;
		nmapDetailResultRepo.upsert(detailResult.getId(), detailResult.getLog());
	}

	@Override
	public String getErrorText(NMapAnalysisJobResult analysisResult) {
		return analysisResult.getNmapLog();
	}
}
