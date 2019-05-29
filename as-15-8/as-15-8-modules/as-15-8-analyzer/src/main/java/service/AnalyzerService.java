package service;

import analysis.AnalysisResult;

/**
 * Интерфейс сервиса анализа резуьтатов проверки мероприятия
 * @author shabalinAI
 * @param <T>
 *
 */
public interface AnalyzerService<T> {
	
	/**
	 * Метод анализа результата работы робота
	 * @param result Результат работы робота
	 * @return
	 */
	AnalysisResult analyzeResult(T execResult);
}
