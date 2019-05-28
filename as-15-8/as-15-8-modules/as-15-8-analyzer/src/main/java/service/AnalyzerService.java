package service;

import analysis.AnalysisResult;
import execution.ExecutionJobResult;

/**
 * Интерфейс сервиса анализа резуьтатов проверки мероприятия
 * @author shabalinAI
 *
 */
public interface AnalyzerService {

	/**
	 * Тип результата выполнения проверки
	 * @return
	 */
	Class<? extends ExecutionJobResult> getExecutionResultType();
	
	/**
	 * Метод анализа результата работы робота
	 * @param result Результат работы робота
	 * @return
	 */
	AnalysisResult analyzeResult(ExecutionJobResult execResult);
}
