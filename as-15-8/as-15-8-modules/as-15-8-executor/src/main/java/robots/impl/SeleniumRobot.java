package robots.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
	protected boolean enableLog;
	
	private CompletableFuture<ExecutionJobResult> currentExecutionFuture;

	@Getter
	private RobotDriverParameters driverParams;

	@Getter
	private Map<AccessToolParameters, String> scriptParams;

	public SeleniumRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this(driverParams, scriptParams, null);
	}

	public SeleniumRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy) {
		this(driverParams, scriptParams, proxy, true);
	}

	public SeleniumRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy, boolean enableLog) {
		setParams(driverParams, scriptParams);
		this.proxy = proxy;
		this.enableLog = enableLog;
		this.driver = createDriver(proxy, enableLog);
	}

	private void setParams(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this.driverParams = driverParams;
		this.scriptParams = scriptParams;
	}

	public ExecutionJobResult run(CheckUnit checkUnit) {
		currentExecutionFuture = CompletableFuture.supplyAsync(() -> {
				try {
					return execute(checkUnit);
				} catch (RobotScriptExecutionException ex) {
					throw new CompletionException(ex);
				}
			});
		return currentExecutionFuture.join();
	}
	
	/**
	 * Метод, запускаюший выполнение скрипта робота
	 * @param checkUnit Ресурс для проверки
	 * @return
	 * @throws RobotScriptExecutionException
	 */
	protected abstract ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException;

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
		if(!currentExecutionFuture.isDone())
			currentExecutionFuture.cancel(true);
		close(driver);
	}

	public void close(WebDriver driver) {
		if (driver != null) {
			driver.quit();
		}
	}

}
