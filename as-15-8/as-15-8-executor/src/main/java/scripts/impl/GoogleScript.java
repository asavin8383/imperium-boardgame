package scripts.impl;

import org.testng.annotations.Test;

import execution.ExecutionPSJobResult;
import scripts.RobotScript;

/**
 * Скрипт проверки ПС Google
 * @author shabalinAI
 *
 */
public class GoogleScript extends RobotScript{

	private static final String GOOGLE_URL = "https://www.google.ru";
	
	@Test
	public void execute() {
		driver.get(GOOGLE_URL);
		driver.manage().window().fullscreen();
		boolean result = driver.getTitle().equals("Google");
		
		ExecutionPSJobResult message = new ExecutionPSJobResult();
		message.setArrangenmentID(Long.parseLong(getArrangenmentID()));
		message.setErdiID(Long.parseLong(getErdiID()));
		message.setCheckUnit(getCheckUnit());
		message.setCheckResult(result);
		sendExecutionResult(message);
	}
}
