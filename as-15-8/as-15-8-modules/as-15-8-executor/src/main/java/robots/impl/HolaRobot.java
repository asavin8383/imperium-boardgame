package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.DriverFactory;
import robots.ProxyUtils;
import robots.exceptions.ExecutionException;
import common.ExecutorProperties;
import robots.utils.HttpResponseHelper;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;
import robots.utils.ScriptUtils.PageResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import static robots.utils.HttpResponseHelper.HttpResponseMeta;


public class HolaRobot extends SeleniumRobot {

    private String crxFilePath;

    protected String stubUrl;
    protected boolean useEtalon;


    public HolaRobot(Map<AccessToolParameter, String> scriptParams) {

    	super(scriptParams,
    			ProxyUtils.getFullProxy(
                        ExecutorProperties.getEtalon().getProxy().getType(),
                        ExecutorProperties.getEtalon().getProxy().getHost(),
                        ExecutorProperties.getEtalon().getProxy().getPort(),
                        ExecutorProperties.getEtalon().getProxy().getUsername(),
                        ExecutorProperties.getEtalon().getProxy().getPassword()
    			)
    		);

        this.useEtalon = ScriptUtils.useEtalon(scriptParams);

     	this.stubUrl = scriptParams.get(AccessToolParameter.STUB_URL);

     	this.crxFilePath = scriptParams.get(AccessToolParameter.CRX_FILE_PATH);
    }

    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new ExecutionException("Ошибка, неверный браузер! Для данного робота поддерживатся только браузер CHROME!");

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

        try {
            driver = DriverFactory.createChromeDriver(
                    ExecutorProperties.getSeleniumHubUrl(),
                    Platform.valueOf(getScriptParams().get(AccessToolParameter.PLATFORM)),
                    getScriptParams().get(AccessToolParameter.APPLICATION),
                    crxFilePath
            );

            WebDriverWait wait = new WebDriverWait(driver, 60);
            wait.until(webDriver -> webDriver != null && webDriver.getWindowHandles().size() > 1);

            ArrayList<String> handles = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(handles.get(1));

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
        return "chrome".equalsIgnoreCase(getScriptParams().get(AccessToolParameter.BROWSER));
    }

}
