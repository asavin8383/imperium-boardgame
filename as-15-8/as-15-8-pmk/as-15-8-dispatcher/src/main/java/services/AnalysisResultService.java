package services;

import enums.CheckUnitJobResult;
import model.Result;
import model.enums.CheckType;

/**
 * Сервис для работы с результатами анализа проверок запрещенных ресурсов
 * @author shabalinAI
 * @param <T>
 *
 */
public interface AnalysisResultService<T> {

	CheckType getCheckType();

	/**
	 * Метод обработки результатов анализа проверок запрещенных ресурсов
	 * @param analysisResult Результат анализа проверок запрещенных ресурсов
	 * @return
	 */
	void saveResult(Result result, T analysisResult);

	String getErrorText(T analysisResult);
	
}
