package services;

import enums.CheckUnitJobResult;
import model.Result;

/**
 * Сервис для работы с результатами анализа проверок запрещенных ресурсов
 * @author shabalinAI
 * @param <T>
 *
 */
public interface AnalysisResultService<T> {
	
	/**
	 * Метод обработки результатов анализа проверок запрещенных ресурсов
	 * @param analysisResult Результат анализа проверок запрещенных ресурсов
	 * @return
	 */
	void processResult(Result result, T analysisResult);
	
}
