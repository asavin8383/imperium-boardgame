package robots.impl;

import java.io.IOException;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.Getter;
import robots.DriverFactory;
import robots.Robot;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public abstract class SeleniumRobot implements Robot {

    protected WebDriver driver;
	protected String proxy;

	@Getter
	private RobotDriverParameters driverParams;

	@Getter
	private Map<AccessToolParameters, String> scriptParams;

	public SeleniumRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this(driverParams, scriptParams, null);
	}

	public SeleniumRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy) {
		setParams(driverParams, scriptParams);
		this.proxy = proxy;
		this.driver = createDriver(proxy);
	}

	private void setParams(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this.driverParams = driverParams;
		this.scriptParams = scriptParams;
	}

	/**
	 * Метод, запускаюший выполнение скрипта робота
	 * @param checkUnit Ресурс для проверки
	 * @return
	 * @throws RobotScriptExecutionException
	 */
	public abstract ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException;

	protected WebDriver createDriver(String proxy) {
		WebDriver driver = DriverFactory.createDriver(
				getDriverParams().getHubURL(),
				getDriverParams().getPlatformName(),
				getDriverParams().getApplicationName(),
				getDriverParams().getBrowserName(),
				proxy
		);
		return driver;
	}

	@Override
	public void close() throws IOException {
		close(driver);
	}

	public void close(WebDriver driver) {
		if (driver != null) {
			driver.quit();
		}
	}

}
