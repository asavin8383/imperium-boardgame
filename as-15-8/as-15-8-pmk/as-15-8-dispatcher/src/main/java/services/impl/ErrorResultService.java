package services.impl;

import analysis.CheckUnitStatusNotification;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
import model.ErrorDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ErrorDetailResultRepo;
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
public class ErrorResultService implements DetailResultService<CheckUnitStatusNotification, ErrorDetailResult> {

	private final ErrorDetailResultRepo errorDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.ERROR;
	}

	@Override
	public ErrorDetailResult create(CheckUnitStatusNotification checkUnitStatusNotification){
		ErrorDetailResult errorDetailResult = new ErrorDetailResult();
		fill(errorDetailResult, checkUnitStatusNotification);
		return errorDetailResult;
	}

	@Override
	public ErrorDetailResult getOrCreate(Result result, CheckUnitStatusNotification checkUnitStatusNotification){
		ErrorDetailResult errorDetailResult = errorDetailResultRepo.findById(result.getId()).orElseGet(ErrorDetailResult::new);
		errorDetailResult.setResult(result);
		fill(errorDetailResult, checkUnitStatusNotification);
		return errorDetailResult;
	}

	private void fill(ErrorDetailResult detailResult, CheckUnitStatusNotification checkUnitResult) {
		detailResult.setError(checkUnitResult.getDescription());
	}

    @Override
	public void save(EntityManager entityManager, DetailResult errorDetailResult) {
		ErrorDetailResult detailResult = (ErrorDetailResult) errorDetailResult;
		errorDetailResultRepo.upsert(detailResult.getId(), detailResult.getError());
	}

    @Override
    public String getErrorText(CheckUnitStatusNotification checkUnitResult) {
        return checkUnitResult.getDescription();
    }
}
