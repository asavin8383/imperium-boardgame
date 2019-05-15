package scripts;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public abstract class RobotScript {

	/** Драйвер selenium */
	protected WebDriver driver;
	
	/**
	 * Метод создания драйвера
	 * @param hubURL URL хаба selenium
	 * @param browserName Имя браузера
	 * @param platformName Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 * @param arrangenmentID Идентификатор мероприятия
	 * @param erdiID Идентификатор ЕРДИ
	 * @param url URL для проверки
	 * @throws MalformedURLException
	 */
	@BeforeClass
	@Parameters({"hubURL", "browserName", "platformName", "applicationName", "arrangenmentID", "erdiID", "url"})
	public void createDriver(
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
	
	/**
	 * Метод закрытия драйвера
	 */
	@AfterClass
	public void closeDriver() {
		if(driver!=null) {
			driver.quit();
		}
	}
	
}
