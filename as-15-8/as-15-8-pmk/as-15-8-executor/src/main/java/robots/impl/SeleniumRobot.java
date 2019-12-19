package robots.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import enums.AccessToolParameter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import execution.ExecutionJobResult;
import lombok.Getter;
import robots.DriverFactory;
import robots.Robot;
import robots.exceptions.Cancel_ExecutionException;
import robots.exceptions.ExecutionException;
import common.ExecutorProperties;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Slf4j
public abstract class SeleniumRobot implements Robot {

    protected WebDriver driver;
	protected String proxy;
	protected boolean enableLog;

	@Getter
	private Map<AccessToolParameter, String> scriptParams;

	public SeleniumRobot(Map<AccessToolParameter, String> scriptParams) {
		this(scriptParams, null);
	}

	public SeleniumRobot(Map<AccessToolParameter, String> scriptParams, String proxy) {
		this(scriptParams, proxy, true);
	}

	public SeleniumRobot(Map<AccessToolParameter, String> scriptParams, String proxy, boolean enableLog) {
		setParams(scriptParams);
		this.proxy = proxy;
		this.enableLog = enableLog;
	}

	private void setParams(Map<AccessToolParameter, String> scriptParams) {
		this.scriptParams = scriptParams;
	}

	public ExecutionJobResult run(CheckUnit checkUnit) throws ExecutionException {
		try {
			this.driver = createDriver(proxy, enableLog, checkUnit.getValue());
			return execute(checkUnit);
		} catch (CancellationException ex) {
			throw new Cancel_ExecutionException(ex);
		} catch (Exception ex) {
			if(ex instanceof ExecutionException)
				throw (ExecutionException)ex;
			else
				throw new ExecutionException("Ошибка при выполнении робота", ex);
		}
	}
	
	/**
	 * Метод, запускаюший выполнение скрипта робота
	 * @param checkUnit Ресурс для проверки
	 * @return
	 * @throws ExecutionException
	 */
	protected abstract ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException;

	protected WebDriver createDriver(String proxy, boolean enableLog, String checkUrl) {
		WebDriver driver = DriverFactory.createDriver(
				ExecutorProperties.getSeleniumHubUrl(),
				Platform.valueOf(getScriptParams().get(AccessToolParameter.PLATFORM).toUpperCase().trim()),
				getScriptParams().get(AccessToolParameter.BROWSER),
				getScriptParams().get(AccessToolParameter.VERSION),
				proxy,
				enableLog
		);
		return driver;
	}

	@Override
	public void destroy() throws IOException {
		close(driver);
	}

	public void close(WebDriver driver) {
		if (driver != null) {
			try {
				driver.quit();
				driver = null;
			} catch (Exception ex){
				log.warn("Ошибка при закрытии драйвера", ex);
			}
		}
	}	
}
