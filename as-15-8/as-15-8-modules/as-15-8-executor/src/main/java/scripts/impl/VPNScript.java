package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import scripts.*;
import scripts.ScriptUtils.PageResult;
import scripts.exceptions.RobotScriptExecutionException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public WebDriver createEtalonDriver() throws RobotScriptExecutionException {
        WebDriver driver = null;
        try {
        	driver = DriverFactory.createDriver(
                    getDriverParams().getHubURL(),
                    getDriverParams().getPlatformName(),
                    getDriverParams().getApplicationName(),
                    getDriverParams().getBrowserName(),
                    etalonProxy
             );

        } catch (Exception e) {
            log.info("Exception on create WebDriver");
            e.printStackTrace();
            throw new RobotScriptExecutionException("Ошибка, создания эталонного драйвера!");
        }
        return driver;
    }

    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new RobotScriptExecutionException("Ошибка, неправильный браузер! Для данного робота поддерживатся только браузер CHROME!");

        String url = ScriptUtils.getCheckUnitValue(checkUnit);

        // получение страницы и доп. данных от основного драйвера
        PageResult pageResult;
        byte[] screenShot;
        String finalUrl;
        try {
            pageResult = loadPage(url, driver, 3);
            screenShot = ScriptUtils.getScreenshot(driver);
            finalUrl = driver.getCurrentUrl();
        }
        finally {
            close(driver);
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
            close(driverEtalon);
        }

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
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

        return message;
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
                log.info("----> Попытка загрузить страницу: {}, error: {}", cnt, pageSourceResult.errorCodeChrome);
        }
        return pageSourceResult;
    }

    public boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getDriverParams().getBrowserName());
    }

}
