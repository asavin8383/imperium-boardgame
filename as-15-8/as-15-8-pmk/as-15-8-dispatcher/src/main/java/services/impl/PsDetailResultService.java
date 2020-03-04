package services.impl;

import analysis.PsAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.PsDetailResult;
import model.Result;
import model.enums.CheckType;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PsDetailResultRepo;
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
public class PsDetailResultService implements DetailResultService<PsAnalysisJobResult, PsDetailResult> {

	private final PsDetailResultRepo psDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PS;
	}

	@Override
	public PsDetailResult create(PsAnalysisJobResult psAnalysisResult) {
		PsDetailResult psDetailResult = new PsDetailResult();
		fill(psDetailResult, psAnalysisResult);
		return psDetailResult;
	}

	@Override
	public PsDetailResult getOrCreate(Result result, PsAnalysisJobResult psAnalysisResult){
		PsDetailResult psDetailResult = psDetailResultRepo.findById(result.getId()).orElseGet(PsDetailResult::new);
		psDetailResult.setResult(result);
		fill(psDetailResult, psAnalysisResult);
		return psDetailResult;
	}

	private void fill(PsDetailResult psDetailResult, PsAnalysisJobResult analysisResult) {
		psDetailResult.setDescription(analysisResult.getDescription());
	}

    @Override
	public void save(EntityManager entityManager, DetailResult psDetailResult) {
		PsDetailResult detailResult = (PsDetailResult) psDetailResult;
		String sql = "insert into results.ps_detail_results " +
			"(result_id, description) " +
			"values " +
			"(:id, :description) " +
			"on conflict(result_id) do update " +
			"set " +
			"result_id = :id, " +
			"description = :description";
		NativeQuery nativeQuery = entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
		nativeQuery.setParameter("id", detailResult.getId());
		nativeQuery.setParameter("description", detailResult.getDescription(), StringType.INSTANCE);
		nativeQuery.executeUpdate();
	}

    @Override
    public String getErrorText(PsAnalysisJobResult analysisResult) {
        return analysisResult.getDescription();
    }
}
