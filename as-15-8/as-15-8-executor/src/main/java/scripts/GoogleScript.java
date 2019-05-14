package scripts;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import robots.DriverFactory;

public class GoogleScript {

	private static final String GOOGLE_URL = "https://www.google.ru";
	
	private WebDriver driver;
	
	@BeforeClass
	@Parameters({"hubURL", "browserName", "platformName", "applicationName"})
	public void setUp(String hubURL, String browserName, String platformName, String applicationName) throws MalformedURLException {
		driver = DriverFactory.createDriver(new URL(hubURL), platformName, applicationName, browserName);
	}
	
	@Test
	public void execute() {
		driver.get(GOOGLE_URL);
		driver.manage().window().fullscreen();
		assertEquals("Google", driver.getTitle());
	}
	
	@AfterClass
	public void tearDown() {
		if(driver!=null) {
			driver.quit();
		}
	}
}
