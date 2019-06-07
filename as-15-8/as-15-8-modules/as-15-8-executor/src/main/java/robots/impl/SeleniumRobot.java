package robots.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import lombok.Getter;
import robots.Robot;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.exceptions.RobotScriptExecutionException;

/**
 * Робот на технологии Selenium
 * @author shabalinAI
 *
 * @param <T>
 */
public abstract class SeleniumRobot implements Robot{

	/** Проверяемая ПС/ПАСД */
	@Getter
	protected AccessToolUnit accessToolUnit;
	
	private Class<? extends RobotScript> scriptClass;
	
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
		this.accessToolUnit = accessToolUnit;
		this.scriptClass = scriptClass;
		this.driverParams = driverParams;
	}

	@Override
	public ExecutionJobResult run(CheckUnit checkUnit, Map<AccessToolParameters, String> accessToolParameters) throws RobotScriptExecutionException {
	
		ExecutionJobResult message = null;
		try(RobotScript script = createScript(accessToolParameters)){
			message = script.execute(checkUnit);
		} catch (Exception ex) {
			if(ex instanceof RobotScriptExecutionException)
				throw (RobotScriptExecutionException)ex;
			else
				throw new RobotScriptExecutionException("Ошибка при выполнении скрипта робота", ex);
		}
		
        message.setCheckUnit(checkUnit);
        message.setAccessToolUnit(accessToolUnit);
        
        return message;
	}
	
	protected abstract Object[] getScriptArgs(Map<AccessToolParameters, String> params);
	
	private RobotScript createScript(Map<AccessToolParameters, String> params) {
		try {
			return (RobotScript)scriptClass
					.getConstructors()[0]
					.newInstance(getScriptArgs(params));
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException("Ошибка при создании скрипта робота", ex);
		}
	}
	
}
