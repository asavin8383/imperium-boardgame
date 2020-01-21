package service;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import org.springframework.context.SmartLifecycle;
import robots.exceptions.ExecutionException;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс сервиса управления роботами
 * @author shabalinAI
 *
 */
public interface CheckUnitVerificationService extends SmartLifecycle {

	Map<AccessToolUnit, List<CheckUnitType>> getSupportedTypes();

	/**
	 * Метод запуска роботов по заданию на проверку ресурса
	 * @param checkUnitJob Задание на ресурса 
	 * @return
	 */
	ExecutionJobResult run(Long jobId, CheckUnitJob checkUnitJob) throws ExecutionException;
	
}
