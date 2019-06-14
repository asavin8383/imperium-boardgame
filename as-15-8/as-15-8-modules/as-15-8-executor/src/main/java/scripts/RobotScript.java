package scripts;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutScriptException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Slf4j
public abstract class RobotScript implements Closeable {

    public static final int PAGE_LOAD_TIMEOUT_SEC = 30;
    private static final int PAGE_LOAD_TIMEOUT_MS = PAGE_LOAD_TIMEOUT_SEC * 1000;
    public static final String TIME_OUT_ERROR = "TIME_OUT";

    protected WebDriver driver;
	protected String proxy;

	@Getter
	private ScriptDriverParameters driverParams;

	@Getter
	private Map<AccessToolParameters, String> scriptParams;

	public RobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this(driverParams, scriptParams, null);
	}

	public RobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy) {
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
		if (driver != null)
			driver.quit();
	}

    public ScriptUtils.PageResult loadPage(String url, WebDriver webDriver, int countRetry) throws RobotScriptExecutionException {
        webDriver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT_SEC, TimeUnit.SECONDS);

        ScriptUtils.PageResult pageSourceResult = null;
        int cnt = 0;

        while (cnt < countRetry && (pageSourceResult == null || pageSourceResult.errorCodeChrome != null)) {
            if (pageSourceResult != null) {
                ScriptUtils.waitDriver(webDriver, 3);
            }
            cnt++;

            try {
                webDriver.get(url);
                webDriver.manage().window().fullscreen();

                ScriptUtils.waitPageLoading(webDriver);
                CloudflareUtils.waitCloudflareRedirect(webDriver, PAGE_LOAD_TIMEOUT_MS);
                ScriptUtils.waitPageLoading(webDriver);

                if (CloudflareUtils.isCloudflareError(webDriver)) {
                    pageSourceResult = new ScriptUtils.PageResult(null, CloudflareUtils.getCloudflareErrorDetailsOpt(webDriver, null));
                } else {
                    pageSourceResult = ScriptUtils.getPageSource(webDriver);
                }
            } catch (TimeoutException | TimeoutScriptException e) {
                log.info("TimeoutException при получении эталона", e);
                pageSourceResult = new ScriptUtils.PageResult(null, TIME_OUT_ERROR);
            } catch (InterruptedException e) {
                throw new RobotScriptExecutionException("Выполнение потока прервано", e);
            }

            if (countRetry > 1) {
                log.info("----> Попытка загрузить страницу: {}, error: {}", cnt, pageSourceResult.errorCodeChrome);
            }
        }
        return pageSourceResult;
    }


}
