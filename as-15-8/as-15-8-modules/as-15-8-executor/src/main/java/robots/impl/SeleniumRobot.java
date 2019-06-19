package robots.impl;

import enums.AccessToolUnit;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;

/**
 * Робот на технологии Selenium
 * @author shabalinAI
 *
 * @param <T>
 */
public abstract class SeleniumRobot extends AbstractRobot {
	
	protected ScriptDriverParameters driverParams;

	/**
	 * Робот на технологии Selenium
	 * @param accessToolUnit Проверяемая ПС/ПАСД
	 * @param hubURL Класс скрипта робота
	 * @param scriptClass URL хаба selenium Grid
	 * @param browserName Имя браузера
	 * @param platform Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 */
	public SeleniumRobot(AccessToolUnit accessToolUnit, Class<? extends RobotScript> scriptClass, ScriptDriverParameters driverParams) {
		super(accessToolUnit, scriptClass);
		this.driverParams = driverParams;
	}
	
}
