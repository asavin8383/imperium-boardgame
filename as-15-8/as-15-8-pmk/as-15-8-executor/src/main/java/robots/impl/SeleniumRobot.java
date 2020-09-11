package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import robots.DriverFactory;
import robots.Robot;
import robots.exceptions.Cancel_ExecutionException;
import robots.exceptions.ExecutionException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Slf4j
public abstract class SeleniumRobot implements Robot {

    private volatile WebDriver driver;
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
		} catch(InterruptedException ex) {
			log.info("Работа робота была прервана: " + checkUnit.getContentId());
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
	 */
	protected abstract ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException, InterruptedException;

	protected WebDriver createDriver(String proxy, boolean enableLog, String checkUrl) {
		return DriverFactory.createDriver(
				ExecutorProperties.getSeleniumHubUrl(),
				Platform.valueOf(getScriptParams().get(AccessToolParameter.PLATFORM).toUpperCase().trim()),
				getScriptParams().get(AccessToolParameter.BROWSER),
				getScriptParams().get(AccessToolParameter.VERSION),
				proxy,
				enableLog
		);
	}

	protected synchronized WebDriver getDriver() throws InterruptedException {
		if(this.driver == null) {
			log.info("Драйвер был закрыт");
			throw new InterruptedException("Робот был остановлен");
		}
		return this.driver;
	}

	protected synchronized void setDriver(WebDriver driver) {
		this.driver = driver;

	}

	@Override
	public void destroy() throws IOException {
		try {
			close(getDriver());
			this.driver = null;
		} catch (InterruptedException ignored) {}
	}

	public void close(WebDriver driver) {
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception ex){
				log.warn("Ошибка при закрытии драйвера", ex);
			}
		}
	}	
}
