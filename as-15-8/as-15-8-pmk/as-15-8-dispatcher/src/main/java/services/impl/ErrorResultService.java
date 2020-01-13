package services.impl;

import analysis.CheckUnitStatusNotification;
import analysis.PS_AnalysisJobResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.ErrorDetailResult;
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
public class ErrorResultService implements AnalysisResultService<CheckUnitStatusNotification> {

	@Override
	public CheckType getCheckType() {
		return CheckType.ERROR;
	}

	@Override
	public DetailResult createDetails(Result result, CheckUnitStatusNotification checkUnitResult) {
		ErrorDetailResult errorDetailResult = new ErrorDetailResult();

		errorDetailResult.setResult(result);

		errorDetailResult.setError(checkUnitResult.getDescription());
		return errorDetailResult;
	}

    @Override
    public String getErrorText(CheckUnitStatusNotification checkUnitResult) {
        return checkUnitResult.getDescription();
    }
}
