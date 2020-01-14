package services.impl;

import analysis.CheckUnitStatusNotification;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.ErrorDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ErrorDetailResultRepo;
import services.DetailResultService;

/**
 * Класс для работы с результатами анализа проверок запрещенных ресурсов в ПС
 * 
 * @author shabalinAI
 *
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErrorResultService implements DetailResultService<CheckUnitStatusNotification> {

	private final ErrorDetailResultRepo errorDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.ERROR;
	}

	@Override
	public DetailResult create(Result result, CheckUnitStatusNotification checkUnitResult) {
		ErrorDetailResult errorDetailResult = errorDetailResultRepo.findById(result.getJobId()).orElseGet(ErrorDetailResult::new);

		errorDetailResult.setResult(result);

		errorDetailResult.setError(checkUnitResult.getDescription());
		return errorDetailResult;
	}

	@Override
	public void save(DetailResult detailResult) {
		if(!(detailResult instanceof ErrorDetailResult))
			throw new AS_15_8_DispatcherException("Ошибка при сохранении детального результата типа " + detailResult.getClass().getSimpleName());
		errorDetailResultRepo.save((ErrorDetailResult) detailResult);
	}

    @Override
    public String getErrorText(CheckUnitStatusNotification checkUnitResult) {
        return checkUnitResult.getDescription();
    }
}
