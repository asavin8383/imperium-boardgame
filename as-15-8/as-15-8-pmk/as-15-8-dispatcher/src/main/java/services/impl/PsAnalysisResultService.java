package services.impl;

import analysis.PS_AnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.PsDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PsDetailResultRepo;
import services.AnalysisResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PsAnalysisResultService implements AnalysisResultService<PS_AnalysisJobResult> {

	private final PsDetailResultRepo psDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PS;
	}

	@Override
	public void saveResult(Result result, PS_AnalysisJobResult analysisResult) {
		PsDetailResult psDetailResult = psDetailResultRepo.findById(result.getId())
				.orElseGet(PsDetailResult::new);

		psDetailResult.setResult(result);

		psDetailResult.setDescription(analysisResult.getDescription());
		psDetailResultRepo.save(psDetailResult);
	}

    @Override
    public String getErrorText(PS_AnalysisJobResult analysisResult) {
        return analysisResult.getDescription();
    }
}
