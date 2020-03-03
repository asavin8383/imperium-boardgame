package services;

import model.DetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

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

	void save(EntityManager entityManager, DetailResult detailResult);

	String getErrorText(T checkUnitResult);
	
}
