package scripts.impl;

import execution.ExecutionPSJobResult;
import scripts.RobotScript;
import scripts.RobotScriptExecutionException;

/**
 * Скрипт проверки ПС Google
 * @author shabalinAI
 *
 */
public class GoogleScript extends RobotScript{

	private static final String GOOGLE_URL = "https://www.google.ru";
	
	@Override
	public void execute() throws RobotScriptExecutionException{
		driver.get(GOOGLE_URL);
		driver.manage().window().fullscreen();
		boolean result = driver.getTitle().equals("Google");
		
		ExecutionPSJobResult message = new ExecutionPSJobResult();
		message.setJobID(Long.parseLong(getJobID()));
		message.setCheckUnit(getCheckUnit());
		message.setCheckResult(result);
		sendExecutionResult(message);
	}
}
