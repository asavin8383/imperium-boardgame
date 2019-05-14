package scripts.impl;

import static org.junit.Assert.assertEquals;

import org.openqa.selenium.WebDriver;

import scripts.RobotScript;

public class GoogleScript implements RobotScript{

	private static final String GOOGLE_URL = "https://www.google.ru";
	
	private WebDriver driver;
	
	@Override
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	@Override
	public void execute() {
		driver.get(GOOGLE_URL);
		driver.manage().window().fullscreen();
		assertEquals("Google", driver.getTitle());
	}
}
