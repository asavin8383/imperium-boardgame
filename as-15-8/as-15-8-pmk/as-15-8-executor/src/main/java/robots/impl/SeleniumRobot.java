package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import robots.DriverFactory;
import robots.Robot;
import robots.exceptions.BadIP_ExecutionExeption;
import robots.exceptions.Cancel_ExecutionException;
import robots.exceptions.Captcha_ExecutionException;
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
		} catch (Captcha_ExecutionException | BadIP_ExecutionExeption ex) {
			throw ex;
		} catch (CancellationException ex) {
			throw new Cancel_ExecutionException(ex);
		} catch (Exception ex) {
			if(ex instanceof ExecutionException)
				throw (ExecutionException)ex;
			else
				throw new ExecutionException("Ошибка при выполнении робота", ex);
		}
	}

	@Retryable(
//		value = {Captcha_ExecutionException.class, BadIP_ExecutionExeption.class, WebDriverException.class},
		value = {Exception.class},
		maxAttempts = 10,
		backoff = @Backoff(delay=3000))
//		maxAttemptsExpression = "${retryExecution.maxAttempts}",
//		backoff = @Backoff(delayExpression = "${retryExecution.maxDelay}"))
	public ExecutionJobResult runWithRetry(CheckUnit checkUnit) {
		log.info("Запуск проверки ресурса: {}", checkUnit.getValue());
		this.driver = createDriver(proxy, enableLog, checkUnit.getValue());
		return execute(checkUnit);
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
