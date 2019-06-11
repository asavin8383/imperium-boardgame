package service;

import checkUnits.CheckUnitJob;
import scripts.exceptions.Captcha_RobotScriptExecutionException;

/**
 * Интерфейс сервиса управления роботами
 * @author shabalinAI
 *
 */
public interface RobotsService {

	/**
	 * Метод запуска роботов по заданию на проверку ресурса
	 * @param checkUnitJob Задание на ресурса 
	 * @return
	 */
	void run(CheckUnitJob checkUnitJob) throws Captcha_RobotScriptExecutionException;
	
}
