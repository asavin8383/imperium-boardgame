package robots.factories.impl;

import enums.AccessToolUnit;
import robots.Robot;
import robots.RobotDriverParameters;

/**
 * Робот на технологии Selenium
 * @author shabalinAI
 *
 * @param <T>
 */
public abstract class SeleniumRobotsFactory extends AbstractRobotsFactory {
	
	protected RobotDriverParameters driverParams;

	/**
	 * Робот на технологии Selenium
	 * @param accessToolUnit Проверяемая ПС/ПАСД
	 * @param hubURL Класс скрипта робота
	 * @param scriptClass URL хаба selenium Grid
	 * @param browserName Имя браузера
	 * @param platform Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 */
	public SeleniumRobotsFactory(AccessToolUnit accessToolUnit, Class<? extends Robot> scriptClass, RobotDriverParameters driverParams) {
		super(accessToolUnit, scriptClass);
		this.driverParams = driverParams;
	}
	
}
