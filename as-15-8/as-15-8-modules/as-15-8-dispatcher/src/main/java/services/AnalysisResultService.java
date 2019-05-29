package services;

import enums.ArrangementUnitCheckResult;

/**
 * Сервис для работы с результатами анализа проверок запрещенных ресурсов
 * @author shabalinAI
 * @param <T>
 *
 */
public interface AnalysisResultService<T> {
	
	/**
	 * Метод обработки результатов анализа проверок запрещенных ресурсов
	 * @param <T>
	 * @param result Результат анализа проверок запрещенных ресурсов
	 * @return
	 */
	ArrangementUnitCheckResult processResult(T result);
	
}
