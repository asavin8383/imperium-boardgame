package service;

import execution.ExecutionJobResult;
import model.ArrangementResult;

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
	 * Метод проверки результата работы робота
	 * @param result Результат работы робота
	 * @return
	 */
	ArrangementResult analyzeResult(ExecutionJobResult result);
	
	/**
	 * Метод записи результата проверки в хранилище
	 * @param result Результат проверки
	 */
	void writeCheckResult(ArrangementResult result);
	
}
