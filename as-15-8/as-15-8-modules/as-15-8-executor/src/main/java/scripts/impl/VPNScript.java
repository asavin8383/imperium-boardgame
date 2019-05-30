package scripts.impl;

import execution.ExecutionVpnJobResult;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.DriverFactory;
import scripts.RobotScript;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;

import static scripts.ScriptUtils.PageResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VPNScript extends RobotScript {

    protected String vpnProxy;
    protected String etalonProxy;
    protected boolean useEtalonProxy;
    protected int timeoutRequest;
    protected int tryCountRequest;

    protected List<WebDriver> proxyDrivers = new ArrayList<>();

    String[] successChromeErrors = {"ERR_EMPTY_RESPONSE"};
    Map<String, String> mapSuccessChromeErrors = new HashMap<>();


    @BeforeClass
    @Parameters({"vpnProxy", "etalonProxy", "useEtalonProxy", "timeoutRequest", "tryCountRequest"})
    public void setOptions(String vpnProxy, String etalonProxy, String useEtalonProxy, String timeoutRequest, String tryCountRequest) {
        this.vpnProxy = vpnProxy;
        this.etalonProxy = etalonProxy;
        this.useEtalonProxy = Boolean.parseBoolean(useEtalonProxy);
        this.timeoutRequest = Integer.parseInt(timeoutRequest);
        this.tryCountRequest = Integer.parseInt(tryCountRequest);

        for (String str : successChromeErrors){
            mapSuccessChromeErrors.put(str, "");
        }
    }

    @AfterClass
    public void closeProxyDrivers() {
        proxyDrivers.forEach(driver -> {
            if (driver != null) {
                driver.quit();
            }
        });
    }

    public WebDriver createProxyDriver(String proxy) {
        WebDriver driver = null;
        try {
            driver = DriverFactory.createDriver(
                    new URL(getHubURL()), getPlatformName(), getApplicationName(), getBrowserName(), proxy);


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
        int tryCount = 3;

        while (tryCount > 0 && (pageSourceResult == null || pageSourceResult.errorCodeChrome != null)){
            if (pageSourceResult != null){
                ScriptUtils.waitDriver(driver, 2);
            }
            tryCount--;

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
        }

        // если нет ошибок, то получаем странцы эталоны
        String pageSourceEtalon = null;
        if (pageSourceResult.errorCodeChrome == null && useEtalonProxy){
            WebDriver etalonDriver = createProxyDriver(etalonProxy);
            etalonDriver.get(url);
            etalonDriver.manage().window().fullscreen();
            ScriptUtils.waitDriver(etalonDriver, 3);

            try {
                ScriptUtils.waitPageLoading(etalonDriver);
            }
            catch (TimeoutException te){
                System.out.println("Timeout exception while access from URL via VPN/Proxy");
                te.printStackTrace();
            }
            PageResult source = ScriptUtils.getPageSource(etalonDriver);
            pageSourceEtalon = source.pageSource;
        }

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        message.setJobID(Long.valueOf(getJobID()));
        message.setCheckUnit(getCheckUnit());

        boolean errorLoadingPage = pageSourceResult.errorCodeChrome != null;

        message.setResponseError(errorLoadingPage);
        message.setChromeErrorCode(pageSourceResult.errorCodeChrome);

        if(pageSourceResult.errorCodeChrome == null && !errorLoadingPage){
            message.setPageContent(pageSourceResult.pageSource);
            message.setScreenshot(ScriptUtils.getScreenshot(driver));
            message.setFinalUrlPage(driver.getCurrentUrl());
            message.setPageContentEtalon(pageSourceEtalon);
        }

        sendExecutionResult(message);
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getBrowserName());
    }

}
