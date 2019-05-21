package service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import enums.ArrangementUnitCheckResult;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import model.ArrangementResult;
import repositories.ArrangementResultRepository;
import service.AnalyzerService;

/**
 * Сервис проверки результата работы робота, проверяющего ПС
 * @author shabalinAI
 *
 */
@Service
public class PS_AnalyzerService implements AnalyzerService {

	@Autowired
	private ArrangementResultRepository repository;
	
	@Override
	public Class<? extends ExecutionJobResult> getExecutionResultType() {
		return ExecutionPSJobResult.class;
	}

	@Override
	public ArrangementResult analyzeResult(ExecutionJobResult result) {
		ArrangementResult arrRes = new ArrangementResult();
		arrRes.setArrangementId(result.getArrangenmentID());
		arrRes.setErdiId(result.getErdiID());
		arrRes.setCheckUnitType(result.getCheckUnit().getType());
		arrRes.setCheckUnitValue(result.getCheckUnit().getValue());
		arrRes.setResult(
				((ExecutionPSJobResult)result).isCheckResult() ? 
						ArrangementUnitCheckResult.COMPLETED : 
						ArrangementUnitCheckResult.INTERNAL_ERROR);
		return arrRes;
	}

	@Override
	public void writeCheckResult(ArrangementResult result) {
		repository.save(result);
	}
}
