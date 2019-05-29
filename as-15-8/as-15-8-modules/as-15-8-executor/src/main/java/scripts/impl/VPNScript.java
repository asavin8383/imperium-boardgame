package scripts.impl;

import execution.ExecutionVpnJobResult;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.DriverFactory;
import scripts.RobotScript;
import scripts.RobotScriptExecutionException;
import scripts.ScriptUtils;
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

        boolean timeoutError = false;

        // получение страницы от VPN (драйвер уже настроен)
        driver.get(getCheckUnit().getValue());
        driver.manage().window().fullscreen();
        ScriptUtils.waitDriver(driver, 3);
        ScriptUtils.waitPageLoading(driver);

        PageResult pageSourceResult = ScriptUtils.getPageSource(driver);

        // если нет ошибок, то получаем странцы эталоны
        String pageSourceEtalon = null;
        if (pageSourceResult.errorCodeChrome == null && useEtalonProxy){
            WebDriver etalonDriver = createProxyDriver(etalonProxy);
            PageResult source = ScriptUtils.getPageSource(etalonDriver);
            pageSourceEtalon = source.pageSource;
        }

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        message.setJobID(Long.valueOf(getJobID()));
        message.setCheckUnit(getCheckUnit());

        message.setTimeoutError(timeoutError);
        message.setChromeErrorCode(pageSourceResult.errorCodeChrome);

        if(pageSourceResult.errorCodeChrome == null && !timeoutError){
            message.setPageContent(pageSourceResult.pageSource);
            message.setScreenShot(ScriptUtils.getScreenshot(driver));
            message.setFinalUrlPage(driver.getCurrentUrl());
            message.setPageContentEtalon(pageSourceEtalon);
        }

        sendExecutionResult(message);
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getBrowserName());
    }

}
