package scripts.impl;

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

        log.info("---------- PROXY -----------");
        log.info("vpnProxy = " + vpnProxy);
        log.info("etalonProxy = " + etalonProxy);
        log.info("stubUrl = " + stubUrl);

        createDriver(vpnProxy);
    }


    @AfterClass
    public void closeProxyDrivers() {
        proxyDrivers.forEach(driver -> {
            if (driver != null) {
                driver.quit();
            }
        });
    }

    public WebDriver createEtalonDriver() {
        WebDriver driver = null;
        try {
            driver = DriverFactory.createDriver(
                    new URL(getHubURL()), getPlatformName(), getApplicationName(), getBrowserName(), etalonProxy);


        } catch (MalformedURLException e) {
            System.out.println("Exception on create WebDriver");
            e.printStackTrace();
        }

        if (driver != null)
            proxyDrivers.add(driver);
        return driver;
    }

    @Test
    public void execute() throws RobotScriptExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new RobotScriptExecutionException("Ошибка, неправильный браузер! Для данного робота поддерживатся только браузер CHROME!");

        String url = getCheckUnit().getValue();

        // получение страницы от VPN (драйвер уже настроен)
        PageResult pageSourceResult = null;
        int tryCount = 3, cnt = 0;

        while (tryCount > 0 && (pageSourceResult == null || pageSourceResult.errorCodeChrome != null)){
            if (pageSourceResult != null){
                ScriptUtils.waitDriver(driver, 3);
            }
            tryCount--; cnt++;

            driver.get(url);
            driver.manage().window().fullscreen();
            ScriptUtils.waitDriver(driver, 3);
            try {
                ScriptUtils.waitPageLoading(driver);
                pageSourceResult = ScriptUtils.getPageSource(driver);
            }
            catch (TimeoutException te){
                pageSourceResult = new PageResult(null, "TIME_OUT");
            }
            log.info("----> try count " + cnt + ", error = " + pageSourceResult.errorCodeChrome);
        }

        // получаем странцу эталона в любом случае (даже если ошибка получения vpn/proxy страницы)
        WebDriver etalonDriver = createEtalonDriver();
        etalonDriver.get(url);
        etalonDriver.manage().window().fullscreen();
        ScriptUtils.waitDriver(etalonDriver, 3);
        PageResult resultEtalon = new PageResult();
        try {
            ScriptUtils.waitPageLoading(etalonDriver);
            resultEtalon = ScriptUtils.getPageSource(etalonDriver);
        }
        catch (TimeoutException te){
            System.out.println("Timeout exception while access from URL via VPN/Proxy");
            te.printStackTrace();
            resultEtalon.errorCodeChrome = "TIME_OUT";
        }

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        fillExecutionResultMessage(message);
        message.setStubUrl(stubUrl);

        message.setResponseError(pageSourceResult.errorCodeChrome != null);

        message.setChromeErrorCode(pageSourceResult.errorCodeChrome);
        if(pageSourceResult.errorCodeChrome == null){
            message.setPageContent(pageSourceResult.pageSource);
            message.setScreenshot(ScriptUtils.getScreenshot(driver));
            message.setFinalUrlPage(driver.getCurrentUrl());
        }

        message.setChromeErrorCodeEtalon(resultEtalon.errorCodeChrome);
        message.setPageContentEtalon(resultEtalon.pageSource);
        if (resultEtalon.errorCodeChrome == null){
            message.setEtalonScreenshot(ScriptUtils.getScreenshot(etalonDriver));
        }

        log.info("--------- message ---------");
        log.info(message.toString());
        //log.info("--------- TEXT ---------");
        //log.info(pageSourceResult.pageSource);

        sendExecutionResult(message);
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getBrowserName());
    }

}
