package robots.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import lombok.Getter;
import robots.Robot;
import scripts.RobotScript;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

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
	
	@Override
	public ExecutionJobResult run(CheckUnit checkUnit, Map<AccessToolParameters, String> accessToolParameters) throws RobotScriptExecutionException {
	
		ExecutionJobResult message = null;
		RobotScript script = null;
		boolean isCaptcha = false;
		try{
			script = createScript(accessToolParameters);
			message = script.execute(checkUnit);
		} catch (Exception ex) {
			if(ex instanceof RobotScriptExecutionException) {
				if(ex instanceof Captcha_RobotScriptExecutionException)
					isCaptcha = true;
				throw (RobotScriptExecutionException)ex;
			} else
				throw new RobotScriptExecutionException("Ошибка при выполнении скрипта робота", ex);
		} finally {
			if(!isCaptcha && script != null) {
				try {
					script.close();
				} catch (IOException ex) {
					throw new RobotScriptExecutionException("Ошибка при закрытии скрипта", ex);
				}
			}
		}
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
