package services.impl;

import analysis.NMapAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.NmapDetailResult;
import model.Result;
import model.enums.CheckType;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
		String sql = "insert into results.nmap_detail_results " +
			"(result_id, log) " +
			"values " +
			"(:id, :log) " +
			"on conflict(result_id) do update " +
			"set " +
			"result_id = :id, " +
			"log = :log";
		NativeQuery nativeQuery = entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
		nativeQuery.setParameter("id", detailResult.getId());
		nativeQuery.setParameter("log", detailResult.getLog(), StringType.INSTANCE);
		nativeQuery.executeUpdate();
	}

	@Override
	public String getErrorText(NMapAnalysisJobResult analysisResult) {
		return analysisResult.getNmapLog();
	}
}
