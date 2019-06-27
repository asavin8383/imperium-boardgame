package service;

import org.springframework.context.SmartLifecycle;

import checkUnits.CheckUnitJob;
import robots.exceptions.Cancel_RobotScriptExecutionException;
import robots.exceptions.Captcha_RobotScriptExecutionException;

/**
 * Интерфейс сервиса управления роботами
 * @author shabalinAI
 *
 */
public interface RobotsService extends SmartLifecycle {

	/**
	 * Метод запуска роботов по заданию на проверку ресурса
	 * @param checkUnitJob Задание на ресурса 
	 * @return
	 */
	void run(CheckUnitJob checkUnitJob) throws Captcha_RobotScriptExecutionException, Cancel_RobotScriptExecutionException;
	
}
