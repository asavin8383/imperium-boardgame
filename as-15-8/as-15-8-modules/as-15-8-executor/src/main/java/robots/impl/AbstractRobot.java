package robots.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import lombok.Getter;
import robots.Robot;
import scripts.RobotScript;

public abstract class AbstractRobot implements Robot {

	/** Проверяемая ПС/ПАСД */
	@Getter
	protected AccessToolUnit accessToolUnit;
	
	private Class<? extends RobotScript> scriptClass;
	
	/**
	 * Робот
	 * @param accessToolUnit Проверяемая ПС/ПАСД
	 * @param scriptClass Класс скрипта
	 */
	public AbstractRobot(AccessToolUnit accessToolUnit, Class<? extends RobotScript> scriptClass) {
		this.accessToolUnit = accessToolUnit;
		this.scriptClass = scriptClass;
	}
	
	protected abstract Object[] getScriptArgs(Map<AccessToolParameters, String> params);
	
	public RobotScript createScript(Map<AccessToolParameters, String> params) {
		try {
			return (RobotScript)scriptClass
					.getConstructors()[0]
					.newInstance(getScriptArgs(params));
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException("Ошибка при создании скрипта робота", ex);
		}
	}
}
