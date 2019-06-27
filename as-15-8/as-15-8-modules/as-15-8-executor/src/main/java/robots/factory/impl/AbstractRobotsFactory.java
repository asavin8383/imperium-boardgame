package robots.factory.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import lombok.Getter;
import robots.Robot;
import robots.factory.RobotsFactory;

public abstract class AbstractRobotsFactory implements RobotsFactory {

	/** Проверяемая ПС/ПАСД */
	@Getter
	protected AccessToolUnit accessToolUnit;
	
	private Class<? extends Robot> scriptClass;
	
	/**
	 * Робот
	 * @param accessToolUnit Проверяемая ПС/ПАСД
	 * @param scriptClass Класс скрипта
	 */
	public AbstractRobotsFactory(AccessToolUnit accessToolUnit, Class<? extends Robot> scriptClass) {
		this.accessToolUnit = accessToolUnit;
		this.scriptClass = scriptClass;
	}
	
	protected abstract Object[] getScriptArgs(Map<AccessToolParameters, String> params);
	
	public Robot createRobot(Map<AccessToolParameters, String> params) {
		try {
			return (Robot)scriptClass
					.getConstructors()[0]
					.newInstance(getScriptArgs(params));
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException("Ошибка при создании скрипта робота", ex);
		}
	}
}
