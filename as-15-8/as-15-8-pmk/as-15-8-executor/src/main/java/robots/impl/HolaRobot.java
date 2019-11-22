package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.ChromeSettings;
import robots.DriverFactory;
import robots.ProxyUtils;
import robots.exceptions.ExecutionException;
import robots.utils.HttpResponseHelper;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;
import robots.utils.ScriptUtils.PageResult;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static robots.utils.HttpResponseHelper.HttpResponseMeta;


public class HolaRobot extends SeleniumRobot {

    private String stubUrl;
    private boolean useEtalon;

    private ChromeSettings.Extension extension;

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

        this.useEtalon =  ExecutorProperties.getEtalon().getEnabled();

     	this.stubUrl = scriptParams.get(AccessToolParameter.STUB_URL);

     	this.extension = new ChromeSettings.Extension(
     	        scriptParams.get(AccessToolParameter.EXTENSION_ID),
                scriptParams.get(AccessToolParameter.EXTENSION_VERSION),
                scriptParams.get(AccessToolParameter.EXTENSION_POPUP)
        );
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
                    Collections.singletonList(extension),
                    checkUnit.getValue()
            );

            // opens empty tab
            // ((JavascriptExecutor) driver).executeScript("window.open()");

//            driver.get(ChromeSettings.Extension.HOLA.getPopupUrl());
//            wait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//div[@class=\"popular-view-footer\"]/a"))).click();

            driver.get("https://hola.org/unblock/popular"); // todo config

            WebDriverWait wait = new WebDriverWait(driver, 60);
            // Конфигурируем холу на доступ для данного URL
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("input")));
            searchBox.sendKeys(url);
            try {
                searchBox.sendKeys(Keys.ENTER);
            } catch (StaleElementReferenceException e) {
                driver.findElement(By.tagName("input")).sendKeys(Keys.ENTER);
            }

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

    private boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getScriptParams().get(AccessToolParameter.BROWSER));
    }

}
