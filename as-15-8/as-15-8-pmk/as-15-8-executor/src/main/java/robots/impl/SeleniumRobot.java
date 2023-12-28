package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import robots.DriverFactory;
import robots.Robot;
import robots.exceptions.ExecutionException;
import robots.exceptions.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

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

	@Override
	public ExecutionJobResult run(CheckUnit checkUnit, long executionTimeout, boolean throwExceptionByCaptchaOrBadIP) throws Throwable {
		try {
			this.driver = createDriver(proxy, enableLog, checkUnit.getValue());

			return CompletableFuture
					.supplyAsync(() -> execute(checkUnit, throwExceptionByCaptchaOrBadIP))
					.applyToEither(timeoutAfter(executionTimeout, TimeUnit.SECONDS), (result) -> result)
					.exceptionally(throwable -> {
						try {
							destroy();
						} catch (IOException ignored) {
						}
						throw new CompletionException(throwable);
					})
					.join();
		} catch (CompletionException compEx) {
			Throwable ex = compEx;
			while(ex.getCause() != null && ex instanceof CompletionException) {
				ex = ex.getCause();
			}
			 if(ex instanceof ExecutionException) {
				 throw ex;
			 } else {
				throw new ExecutionException("Ошибка при выполнении робота", ex);
			}
		}
	}

	private <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
		CompletableFuture<T> result = new CompletableFuture<>();
		ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
		timeoutService.schedule(() -> result.completeExceptionally(new Timeout_ExecutionException()), timeout, unit);
		return result;
	}

	/**
	 * Метод, запускаюший выполнение скрипта робота
	 * @param checkUnit Ресурс для проверки
	 * @return
	 * @throws ExecutionException
	 */
	protected abstract ExecutionJobResult execute(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException;

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
