package robots;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverFactory {
	
	public static WebDriver createDriver(URL hubURL, String platformName, String appName, String browserName) {
		Capabilities cpb = buildCapability(Platform.valueOf(platformName), appName, browserName);
		return new RemoteWebDriver(hubURL, cpb);
	}
	
	private static DesiredCapabilities buildCapability(Platform platform, String appName, String browserName) {
		DesiredCapabilities capability = createCapabilities(browserName);
		capability.setBrowserName(browserName);
		capability.setPlatform(platform);
		capability.setCapability("applicationName", appName);
		return capability;
	}
	
	private static DesiredCapabilities createCapabilities(String browserName) {
		switch(browserName.toLowerCase().trim()) {
			case "chrome":
				return DesiredCapabilities.chrome();
			case "firefox":
				return DesiredCapabilities.firefox();
			case "edge":
				return DesiredCapabilities.edge();
			case "internetexplorer":
				return DesiredCapabilities.internetExplorer();
			case "opera":
				return DesiredCapabilities.operaBlink();
			case "safary":
				return DesiredCapabilities.safari();
			default:
				throw new IllegalArgumentException("Ошибка: браузер не поддерживается: "+browserName);
		}
	}
}
