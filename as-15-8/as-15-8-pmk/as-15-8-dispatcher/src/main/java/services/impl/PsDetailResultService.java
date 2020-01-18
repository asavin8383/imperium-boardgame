package services.impl;

import analysis.PsAnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.PsDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PsDetailResultRepo;
import services.DetailResultService;

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
	public void save(PsDetailResult psDetailResult) {
		psDetailResultRepo.save(psDetailResult);
	}

    @Override
    public String getErrorText(PsAnalysisJobResult analysisResult) {
        return analysisResult.getDescription();
    }
}
