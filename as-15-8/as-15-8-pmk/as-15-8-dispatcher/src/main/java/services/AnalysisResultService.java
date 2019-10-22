package services;

import enums.CheckUnitJobResult;

/**
 * Сервис для работы с результатами анализа проверок запрещенных ресурсов
 * @author shabalinAI
 * @param <T>
 *
 */
public interface AnalysisResultService<T> {
	
	/**
	 * Метод обработки результатов анализа проверок запрещенных ресурсов
	 * @param result Результат анализа проверок запрещенных ресурсов
	 * @return
	 */
	CheckUnitJobResult processResult(T result);
	
}
