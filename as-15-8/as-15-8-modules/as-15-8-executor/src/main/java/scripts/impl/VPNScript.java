package scripts.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import scripts.DriverFactory;
import scripts.ProxyUtils;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.ScriptUtils.PageResult;
import scripts.exceptions.RobotScriptExecutionException;

@Slf4j
public class VPNScript extends RobotScript {


	protected String vpnProxy;
    protected String etalonProxy;
    protected String stubUrl;
    
    protected List<WebDriver> proxyDrivers = new ArrayList<>();

    public static final String TIME_OUT_ERROR = "TIME_OUT";

    public VPNScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams)
    		throws MalformedURLException {
    	
    	super(driverParams, scriptParams,
    			ProxyUtils.getFullProxy(
					scriptParams.get(AccessToolParameters.PROXY_TYPE),
					scriptParams.get(AccessToolParameters.PROXY_DNS_NAME),
					scriptParams.get(AccessToolParameters.PROXY_PORT),
					scriptParams.get(AccessToolParameters.PROXY_USER),
					scriptParams.get(AccessToolParameters.PROXY_PASSWORD)
    			)
    		);
    	 
    	etalonProxy = ProxyUtils.getFullProxy(
    			scriptParams.get(AccessToolParameters.PROXY_TYPE),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
			);
     	this.stubUrl = scriptParams.get(AccessToolParameters.STUB_URL);
     	
     	log.info("---------- PROXY -----------");
        log.info("vpnProxy = " + vpnProxy);
        log.info("etalonProxy = " + etalonProxy);
        log.info("stubUrl = " + stubUrl);
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
        driver = DriverFactory.createDriver(
                getDriverParams().getHubURL(),
                getDriverParams().getPlatformName(),
                getDriverParams().getApplicationName(),
                getDriverParams().getBrowserName(),
                etalonProxy
         );

        if (driver != null)
            proxyDrivers.add(driver);
        return driver;
    }

    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new RobotScriptExecutionException("Ошибка, неправильный браузер! Для данного робота поддерживатся только браузер CHROME!");

        String url = ScriptUtils.getCheckUnitValue(checkUnit);

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
                pageSourceResult = new PageResult(null, TIME_OUT_ERROR);
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
            resultEtalon.errorCodeChrome = TIME_OUT_ERROR;
        }

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        message.setStubUrl(stubUrl);

        message.setResponseError(pageSourceResult.errorCodeChrome != null);

        message.setChromeErrorCode(pageSourceResult.errorCodeChrome);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        if(pageSourceResult.errorCodeChrome == null){
            message.setPageContent(pageSourceResult.pageSource);
            message.setFinalUrlPage(driver.getCurrentUrl());
        }

        message.setChromeErrorCodeEtalon(resultEtalon.errorCodeChrome);
        message.setPageContentEtalon(resultEtalon.pageSource);
        message.setEtalonScreenshot(ScriptUtils.getScreenshot(etalonDriver));

        log.info("--------- message ---------");
        log.info(message.toString());
        //log.info("--------- TEXT ---------");
        //log.info(pageSourceResult.pageSource);

        return message;
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getDriverParams().getBrowserName());
    }

}
