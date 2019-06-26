package service;

import java.io.IOException;

import checkUnits.CheckUnitJob;
import enums.AccessToolUnit;
import robots.exceptions.Captcha_RobotScriptExecutionException;

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

	void destroyRobot(AccessToolUnit accessToolUnit) throws IOException;
	
}
