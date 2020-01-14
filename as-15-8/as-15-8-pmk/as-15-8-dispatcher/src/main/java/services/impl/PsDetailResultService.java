package services.impl;

import analysis.PS_AnalysisJobResult;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
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
public class PsDetailResultService implements DetailResultService<PS_AnalysisJobResult> {

	private final PsDetailResultRepo psDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PS;
	}

	@Override
	public DetailResult create(Result result, PS_AnalysisJobResult analysisResult) {
		PsDetailResult psDetailResult = psDetailResultRepo.findById(result.getJobId()).orElse(new PsDetailResult());

		psDetailResult.setResult(result);

		psDetailResult.setDescription(analysisResult.getDescription());
		return psDetailResult;
	}

	@Override
	public void save(DetailResult detailResult) {
		if(!(detailResult instanceof PsDetailResult))
			throw new AS_15_8_DispatcherException("Ошибка при сохранении детального результата типа " + detailResult.getClass().getSimpleName());
		psDetailResultRepo.save((PsDetailResult) detailResult);
	}

    @Override
    public String getErrorText(PS_AnalysisJobResult analysisResult) {
        return analysisResult.getDescription();
    }
}
