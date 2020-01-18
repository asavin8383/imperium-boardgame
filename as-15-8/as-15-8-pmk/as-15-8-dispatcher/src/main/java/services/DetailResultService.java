package services;

import model.Result;
import model.enums.CheckType;

/**
 * Сервис для работы с результатами анализа проверок запрещенных ресурсов
 * @author shabalinAI
 * @param <T>
 *
 */
public interface DetailResultService<T, R> {

	CheckType getCheckType();

	R create(T analysisResult);

	R getOrCreate(Result result, T analysisResult);

	void save(R detailResult);

	String getErrorText(T checkUnitResult);
	
}
