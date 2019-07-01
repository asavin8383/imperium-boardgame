package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.DriverFactory;
import robots.ProxyUtils;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;
import robots.utils.HttpResponseHelper;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;
import robots.utils.ScriptUtils.PageResult;

import java.util.Map;
import java.util.function.Function;

import static robots.utils.HttpResponseHelper.HttpResponseMeta;


public class HolaRobot extends SeleniumRobot {

    private static final String CHROME_PROFILE = "C:\\Selenium\\Chrome";
    private static final String HOLA_POPUP = "chrome-extension://gkojfkhlekighikafcpjkiklfbnlmeio/js/popup.html";
    protected String stubUrl;
    protected boolean useEtalon;


    public HolaRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {

    	super(driverParams, scriptParams,
    			ProxyUtils.getFullProxy(
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_TYPE),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
    			)
    		);

        this.useEtalon = ScriptUtils.useEtalon(scriptParams);

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
        try {
            if (useEtalon) {
                PageResult pageResult = RobotScriptUtils.loadPage(url, driver);
                HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver);
                if (responseMeta != null){
                    message.setHttpStatus(responseMeta.status);
                    message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
                }
                byte[] screenShot = ScriptUtils.getScreenshot(driver);
                String finalUrl = ScriptUtils.getCurrentUrl(driver);

                message.setChromeErrorCodeEtalon(pageResult.errorCodeChrome);
                message.setPageContentEtalon(pageResult.pageSource);
                message.setEtalonScreenshot(screenShot);
                if (pageResult.errorCodeChrome == null) {
                    message.setFinalUrlPageEtalon(finalUrl);
                }
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

            driver.manage().window().maximize();
            PageResult pageResult = RobotScriptUtils.loadPage(url, driver);
            HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver);
            if (responseMeta != null){
                message.setHttpStatus(responseMeta.status);
                message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
            }

            byte[] screenShot = ScriptUtils.getScreenshot(driver);
            String finalUrl = ScriptUtils.getCurrentUrl(driver);
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



    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getDriverParams().getBrowserName());
    }

}
