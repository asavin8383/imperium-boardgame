package scripts.impl;

import static org.junit.Assert.assertEquals;

import org.testng.annotations.Test;

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
		assertEquals("Google", driver.getTitle());
	}
}
