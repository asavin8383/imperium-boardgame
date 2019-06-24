package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import scripts.DriverFactory;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.exceptions.RobotScriptExecutionException;

import java.io.IOException;
import java.util.Map;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public abstract class SeleniumRobotScript implements RobotScript {

    protected WebDriver driver;
	protected String proxy;
	protected boolean enableLog;

	@Getter
	private ScriptDriverParameters driverParams;

	@Getter
	private Map<AccessToolParameters, String> scriptParams;

	public SeleniumRobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this(driverParams, scriptParams, null);
	}

	public SeleniumRobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy) {
		this(driverParams, scriptParams, proxy, true);
	}

	public SeleniumRobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy, boolean enableLog) {
		setParams(driverParams, scriptParams);
		this.proxy = proxy;
		this.enableLog = enableLog;
		this.driver = createDriver(proxy, enableLog);
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

	protected WebDriver createDriver(String proxy, boolean enableLog) {
		WebDriver driver = DriverFactory.createDriver(
				getDriverParams().getHubURL(),
				getDriverParams().getPlatformName(),
				getDriverParams().getApplicationName(),
				getDriverParams().getBrowserName(),
				proxy, enableLog
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
