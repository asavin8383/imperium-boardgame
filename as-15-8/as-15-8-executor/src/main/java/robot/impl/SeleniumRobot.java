package robot.impl;

import java.net.URL;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import enums.AccessToolUnit;
import lombok.Getter;
import robot.Robot;
import scripts.RobotScript;

public class SeleniumRobot implements Robot{

	@Getter
	private AccessToolUnit accessToolUnit;

	@Getter
	private Class<? extends RobotScript> robotScriptClass;
	
	private DesiredCapabilities capability;
	
	private URL hubURL;
	
	public SeleniumRobot(AccessToolUnit accessToolUnit, Class<? extends RobotScript> robotScriptClass, URL hubUrl, Platform platform, String appName, String browserName) {
		this.accessToolUnit = accessToolUnit;
		this.robotScriptClass = robotScriptClass;
		this.hubURL = hubUrl;
		this.capability = createCapability(platform, appName, browserName);
	}
	
	public boolean run() {
		WebDriver driver = null;
		try {
			driver = new RemoteWebDriver(hubURL, capability);
			RobotScript script = robotScriptClass.newInstance();
			script.setDriver(driver);
			script.execute();
			return true;
		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if(driver != null)
				driver.quit();
		}
	}
	
	private DesiredCapabilities createCapability(Platform platform, String appName, String browserName) {
		DesiredCapabilities capability = createCapabilities(browserName);
		capability.setBrowserName(browserName);
		capability.setPlatform(platform);
		capability.setCapability("applicationName", appName);
		return capability;
	}
	
	private DesiredCapabilities createCapabilities(String browserName) {
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

	@Override
	public WebDriver getDriver() {
		return new RemoteWebDriver(hubURL, capability);
	}
}
