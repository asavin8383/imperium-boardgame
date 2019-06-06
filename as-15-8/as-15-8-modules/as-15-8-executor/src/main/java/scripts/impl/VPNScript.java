package scripts.impl;

import enums.AccessToolUnit;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.DriverFactory;
import scripts.ProxyUtils;
import scripts.RobotScript;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;

import static scripts.ScriptUtils.PageResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VPNScript extends RobotScript {

    protected String vpnProxy;
    protected String etalonProxy;
    protected String stubUrl;
    
    protected List<WebDriver> proxyDrivers = new ArrayList<>();

    public static final String TIME_OUT_ERROR = "TIME_OUT";

    protected boolean needAutoCreateDriver(){
        return false;
    }

    @BeforeClass
    @Parameters({"PROXY_TYPE", "PROXY_DNS_NAME", "PROXY_PORT", "PROXY_USER", "PROXY_PASSWORD",
            "ETALON_PROXY_HOST", "ETALON_PROXY_PORT", "ETALON_PROXY_USERNAME", "ETALON_PROXY_PASSWORD",
            "STUB_URL"})
    public void setParameters(
    	@Optional String proxyType,
    	@Optional String proxyHost,
    	@Optional String proxyPort,
    	@Optional String proxyUser,
    	@Optional String proxyPassword,
        @Optional String etalonProxyHost,
        @Optional String etalonProxyPort,
        @Optional String etalonProxyUser,
        @Optional String etalonProxyPassword,
    	@Optional String stubUrl) throws MalformedURLException {

        vpnProxy = ProxyUtils.getFullProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
        etalonProxy = ProxyUtils.getFullProxy(proxyType, etalonProxyHost, etalonProxyPort, etalonProxyUser, etalonProxyPassword);
    	this.stubUrl = stubUrl;

        // todo - хардккод, удалить!
        proxyHard();

        log.info("---------- PROXY -----------");
        log.info("vpnProxy = " + vpnProxy);
        log.info("etalonProxy = " + etalonProxy);
        log.info("stubUrl = " + stubUrl);
    }

    // todo - хардккод, удалить!
    protected void proxyHard(){
        if (getAccessToolUnit() == AccessToolUnit.TORGUARD){
            vpnProxy = "http://:@192.168.5.10:3128";
            etalonProxy = null;
        }
        else if (getAccessToolUnit() == AccessToolUnit.KASPERSKY){
            vpnProxy = "http://:@192.168.5.194:3128";
            etalonProxy = null;
            stubUrl = "kaspersky.ru";
        }
        else if (getAccessToolUnit() == AccessToolUnit.EXPRESS){
            vpnProxy = "http://:@192.168.5.194:3128";
            etalonProxy = null;
            stubUrl = "expressvpn.com";
        }
    }

    /**
     * Метод закрытия драйвера
     */
    public void closeDriver(WebDriver webDriver) {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    public WebDriver createEtalonDriver() throws RobotScriptExecutionException {
        WebDriver driver = null;
        try {
            driver = DriverFactory.createDriver(
                    new URL(getHubURL()), getPlatformName(), getApplicationName(), getBrowserName(), etalonProxy);

        } catch (MalformedURLException e) {
            log.info("Exception on create WebDriver");
            e.printStackTrace();
            throw new RobotScriptExecutionException("Ошибка, создания эталонного драйвера!");
        }
        return driver;
    }

    public void execute() throws RobotScriptExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new RobotScriptExecutionException("Ошибка, неправильный браузер! Для данного робота поддерживатся только браузер CHROME!");

        String url = ScriptUtils.getCheckUnitValue(getCheckUnit());

        // получение страницы и доп. данных от основного драйвера
        PageResult pageResult;
        byte[] screenShot;
        String finalUrl;
        try {
            createDriver(vpnProxy);
            pageResult = loadPage(url, driver, 3);
            screenShot = ScriptUtils.getScreenshot(driver);
            finalUrl = driver.getCurrentUrl();
        }
        finally {
            closeDriver(driver);
        }

        // получение странцы и доп. данных от эталонного драйвера
        PageResult pageResultEtalon;
        byte[] screenShotEtalon;
        WebDriver driverEtalon = null;
        try {
            // получение страницы и доп. данных от основного драйвера
            driverEtalon = createEtalonDriver();
            pageResultEtalon = loadPage(url, driverEtalon, 1);
            screenShotEtalon = ScriptUtils.getScreenshot(driverEtalon);
        }
        finally {
            closeDriver(driverEtalon);
        }

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        fillExecutionResultMessage(message);

        message.setStubUrl(stubUrl);
        message.setResponseError(pageResult.errorCodeChrome != null);
        message.setChromeErrorCode(pageResult.errorCodeChrome);
        message.setPageContent(pageResult.pageSource);
        message.setScreenshot(screenShot);
        if(pageResult.errorCodeChrome == null){
            message.setFinalUrlPage(finalUrl);
        }

        message.setChromeErrorCodeEtalon(pageResultEtalon.errorCodeChrome);
        message.setPageContentEtalon(pageResultEtalon.pageSource);
        message.setEtalonScreenshot(screenShotEtalon);

        //log.info("--------- message ---------");
        //log.info(message.toString());
        //log.info("--------- TEXT ---------");
        //log.info(pageSourceResult.pageSource);

        sendExecutionResult(message);
    }

    public PageResult loadPage(String url, WebDriver webDriver, int countRetry){
        PageResult pageSourceResult = null;
        int cnt = 0;

        while (cnt < countRetry && (pageSourceResult == null || pageSourceResult.errorCodeChrome != null)){
            if (pageSourceResult != null){
                ScriptUtils.waitDriver(webDriver, 3);
            }
            cnt++;

            webDriver.get(url);
            webDriver.manage().window().fullscreen();
            ScriptUtils.waitDriver(webDriver, 3);
            try {
                ScriptUtils.waitPageLoading(webDriver);
                pageSourceResult = ScriptUtils.getPageSource(webDriver);
            }
            catch (TimeoutException te){
                pageSourceResult = new PageResult(null, TIME_OUT_ERROR);
                log.info("Timeout exception при получении страницы");
            }

            if (countRetry > 1)
                log.info("----> Попытка загрузить страницу: {}, error: {}, jobId: %d", cnt, pageSourceResult.errorCodeChrome, jobID);
        }
        return pageSourceResult;
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getBrowserName());
    }

}
