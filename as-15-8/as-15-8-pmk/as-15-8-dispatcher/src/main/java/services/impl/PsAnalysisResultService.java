package services.impl;

import analysis.PS_AnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.PsDetailResult;
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
public class PsAnalysisResultService implements AnalysisResultService<PS_AnalysisJobResult> {

	@Override
	public CheckType getCheckType() {
		return CheckType.PS;
	}

	@Override
	public DetailResult createDetails(Result result, PS_AnalysisJobResult analysisResult) {
		PsDetailResult psDetailResult = new PsDetailResult();

		psDetailResult.setResult(result);

		psDetailResult.setDescription(analysisResult.getDescription());
		return psDetailResult;
	}

    @Override
    public String getErrorText(PS_AnalysisJobResult analysisResult) {
        return analysisResult.getDescription();
    }
}
