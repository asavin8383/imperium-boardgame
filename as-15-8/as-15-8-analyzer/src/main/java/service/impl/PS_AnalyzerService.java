package service.impl;

import org.springframework.stereotype.Service;

import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import service.AnalyzerService;

/**
 * Сервис проверки результата работы робота, проверяющего ПС
 * @author shabalinAI
 *
 */
@Service
public class PS_AnalyzerService implements AnalyzerService {

	@Override
	public Class<? extends ExecutionJobResult> getExecutionResultType() {
		return ExecutionPSJobResult.class;
	}

	@Override
	public boolean analyzeResult(ExecutionJobResult result) {
		return ((ExecutionPSJobResult)result).isCheckResult();
	}

	@Override
	public void writeCheckResult(boolean result) {
		// TODO Auto-generated method stub
		
	}
}
