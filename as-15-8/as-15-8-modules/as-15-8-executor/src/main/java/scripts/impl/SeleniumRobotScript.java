package scripts.impl;

import java.io.IOException;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.Getter;
import scripts.DriverFactory;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.exceptions.RobotScriptExecutionException;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public abstract class SeleniumRobotScript implements RobotScript {

    protected WebDriver driver;
	protected String proxy;

	@Getter
	private ScriptDriverParameters driverParams;

	@Getter
	private Map<AccessToolParameters, String> scriptParams;

	public SeleniumRobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this(driverParams, scriptParams, null);
	}

	public SeleniumRobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy) {
		setParams(driverParams, scriptParams);
		this.proxy = proxy;
		this.driver = createDriver(proxy);
	}

	private void setParams(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
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
			driver.close();
			driver.quit();
		}
	}

}
