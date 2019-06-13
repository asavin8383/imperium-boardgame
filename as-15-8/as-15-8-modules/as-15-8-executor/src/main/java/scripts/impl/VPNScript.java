package scripts.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import scripts.ProxyUtils;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.ScriptUtils.PageResult;
import scripts.exceptions.RobotScriptExecutionException;

@Slf4j
public class VPNScript extends RobotScript {

    protected boolean useEtalon;
    protected String etalonProxy;
    protected String stubUrl;


    public static final String TIME_OUT_ERROR = "TIME_OUT";

    public VPNScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {

    	super(driverParams, scriptParams,
    			ProxyUtils.getFullProxy(
					scriptParams.get(AccessToolParameters.PROXY_TYPE),
					scriptParams.get(AccessToolParameters.PROXY_DNS_NAME),
					scriptParams.get(AccessToolParameters.PROXY_PORT),
					scriptParams.get(AccessToolParameters.PROXY_USER),
					scriptParams.get(AccessToolParameters.PROXY_PASSWORD)
    			)
    		);

    	String useEtalon = scriptParams.get(AccessToolParameters.USE_ETALON);
    	this.useEtalon = StringUtils.isEmpty(useEtalon) ||
                useEtalon.equalsIgnoreCase("true") ||
                useEtalon.equalsIgnoreCase("on");

    	etalonProxy = ProxyUtils.getFullProxy(
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_TYPE),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
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

        CompletableFuture<Void> pageGetter = CompletableFuture
                .runAsync(() -> {
                    WebDriver driver = this.driver;
                    try {
                        PageResult pageResult = loadPage(url, driver, 3);
                        byte[] screenShot = ScriptUtils.getScreenshot(driver);
                        String finalUrl = driver.getCurrentUrl();

                        message.setResponseError(pageResult.errorCodeChrome != null);
                        message.setChromeErrorCode(pageResult.errorCodeChrome);
                        message.setPageContent(pageResult.pageSource);
                        message.setScreenshot(screenShot);
                        if(pageResult.errorCodeChrome == null){
                            message.setFinalUrlPage(finalUrl);
                        }
                    }
                    finally {
                        close(driver);
                    }
                });

        CompletableFuture<Void> pageGetterEtalon = CompletableFuture
                .runAsync(() -> {
                    if (useEtalon){
                        WebDriver driver = null;
                        try {
                            driver = createDriver(etalonProxy);
                            PageResult pageResult = loadPage(url, driver, 1);
                            byte[] screenShot = ScriptUtils.getScreenshot(driver);
                            String finalUrl = driver.getCurrentUrl();

                            message.setChromeErrorCodeEtalon(pageResult.errorCodeChrome);
                            message.setPageContentEtalon(pageResult.pageSource);
                            message.setEtalonScreenshot(screenShot);
                            if(pageResult.errorCodeChrome == null){
                                message.setFinalUrlPageEtalon(finalUrl);
                            }
                        }
                        finally {
                            close(driver);
                        }
                    }
                });

        CompletableFuture<Void> allPageGetters = CompletableFuture.allOf(pageGetter, pageGetterEtalon);

        try {
            allPageGetters.join();
        }
        catch(CompletionException ex) {
            try {
                throw ex.getCause();
            }
            catch(Error|RuntimeException possible) {
                throw possible;
            }
            catch(Throwable impossible) {
                throw new RobotScriptExecutionException(impossible);
            }
        }

        message.setUseEtalon(this.useEtalon);

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
