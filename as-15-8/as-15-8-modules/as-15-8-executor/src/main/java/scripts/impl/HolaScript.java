package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scripts.*;
import scripts.ScriptUtils.PageResult;
import scripts.exceptions.RobotScriptExecutionException;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class HolaScript extends RobotScript {

    private static final String CHROME_PROFILE = "C:\\Selenium\\Chrome";
    private static final String HOLA_POPUP = "chrome-extension://gkojfkhlekighikafcpjkiklfbnlmeio/js/popup.html";
    protected String stubUrl;


    public static final String TIME_OUT_ERROR = "TIME_OUT";

    public HolaScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {

    	super(driverParams, scriptParams,
    			ProxyUtils.getFullProxy(
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_TYPE),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
    			)
    		);

     	this.stubUrl = scriptParams.get(AccessToolParameters.STUB_URL);
    }

    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new RobotScriptExecutionException("Ошибка, неверный браузер! Для данного робота поддерживатся только браузер CHROME!");

        String url = ScriptUtils.getCheckUnitValue(checkUnit);

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        message.setStubUrl(stubUrl);


        // Эталонная страница с дефолтного драйвера с дефолтным прокси
        WebDriver driver = this.driver;
        try {
            PageResult pageResult = loadPage(url, driver, 3);
            byte[] screenShot = ScriptUtils.getScreenshot(driver);
            String finalUrl = driver.getCurrentUrl();

            message.setChromeErrorCodeEtalon(pageResult.errorCodeChrome);
            message.setPageContentEtalon(pageResult.pageSource);
            message.setEtalonScreenshot(screenShot);
            if (pageResult.errorCodeChrome == null) {
                message.setFinalUrlPageEtalon(finalUrl);
            }
        } finally {
            close(driver);
        }


        // Тестируемая страница через Hola
        try {
            driver = DriverFactory.createChromeDriver(
                getDriverParams().getHubURL(),
                getDriverParams().getPlatformName(),
                getDriverParams().getApplicationName(),
                CHROME_PROFILE
            );

            driver.get(HOLA_POPUP); //собственное открытие страницы расширения (ВОЗМОЖНО для другой версии расширения ID будет другой)

            WebDriverWait wait = new WebDriverWait(driver, 60);
            // Конфигурируем холу на доступ для данного URL
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("input")));
            searchBox.sendKeys(url);
            searchBox.sendKeys(Keys.ENTER);

            // Ждем, пока закончаться все редиректы
            Function<? super WebDriver, ?> pageLoaded =
                    (Function<WebDriver, Boolean>) webDriver ->
                            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete");
            wait.until(pageLoaded);
            wait.until(pageLoaded);

            PageResult pageResult = loadPage(url, driver, 3);
            byte[] screenShot = ScriptUtils.getScreenshot(driver);
            String finalUrl = driver.getCurrentUrl();

            message.setResponseError(pageResult.errorCodeChrome != null);
            message.setChromeErrorCode(pageResult.errorCodeChrome);
            message.setPageContent(pageResult.pageSource);
            message.setScreenshot(screenShot);
            if (pageResult.errorCodeChrome == null) {
                message.setFinalUrlPage(finalUrl);
            }
        } finally {
            close(driver);
        }

        return message;
    }


    public PageResult loadPage(String url, WebDriver webDriver, int countRetry) {
        PageResult pageSourceResult = null;
        int cnt = 0;

        while (cnt < countRetry && (pageSourceResult == null || pageSourceResult.errorCodeChrome != null)) {
            if (pageSourceResult != null) {
                ScriptUtils.waitDriver(webDriver, 3);
            }
            cnt++;

            webDriver.get(url);
            webDriver.manage().window().fullscreen();
            ScriptUtils.waitDriver(webDriver, 3);
            try {
                ScriptUtils.waitPageLoading(webDriver);
                pageSourceResult = ScriptUtils.getPageSource(webDriver);
            } catch (TimeoutException te) {
                pageSourceResult = new PageResult(null, TIME_OUT_ERROR);
                log.info("Timeout exception при получении страницы");
            }

            if (countRetry > 1) log.info("----> Попытка загрузить страницу: {}, error: {}", cnt, pageSourceResult.errorCodeChrome);
        }
        return pageSourceResult;
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getDriverParams().getBrowserName());
    }

}
