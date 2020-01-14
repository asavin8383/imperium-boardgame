package services;

import model.DetailResult;
import model.Result;
import model.enums.CheckType;

/**
 * Сервис для работы с результатами анализа проверок запрещенных ресурсов
 * @author shabalinAI
 * @param <T>
 *
 */
public interface DetailResultService<T> {

	CheckType getCheckType();

	/**
	 * Метод обработки результатов анализа проверок запрещенных ресурсов
	 * @param checkUnitResult Результат анализа проверок запрещенных ресурсов
	 * @return
	 */
	DetailResult create(Result result, T checkUnitResult);

	void save(DetailResult detailResult);

	String getErrorText(T checkUnitResult);
	
}
