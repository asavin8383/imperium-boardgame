package scripts;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import robots.DriverFactory;

public abstract class RobotScript {

	protected WebDriver driver;
	
	@BeforeClass
	@Parameters({"hubURL", "browserName", "platformName", "applicationName", "arrangenmentID", "erdiID", "url"})
	public void setUp(
			String hubURL,
			String browserName,
			String platformName,
			String applicationName,
			String arrangenmentID,
			String erdiID,
			String url
			) throws MalformedURLException {
		
		driver = DriverFactory.createDriver(new URL(hubURL), platformName, applicationName, browserName);
	}
	
	@AfterClass
	public void tearDown() {
		if(driver!=null) {
			driver.quit();
		}
	}
	
}
