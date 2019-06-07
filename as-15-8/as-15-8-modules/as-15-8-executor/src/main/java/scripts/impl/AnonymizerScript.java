package scripts.impl;

import java.net.MalformedURLException;
import java.util.Map;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

public abstract class AnonymizerScript extends VPNScript {

    private static final long DEFAULT_INPUT_DELAY = 0;

    private long inputDelay;

    public AnonymizerScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, long inputDelay)
    		throws MalformedURLException {
    	super(driverParams, scriptParams);
        this.inputDelay = inputDelay <= 0 ?
                DEFAULT_INPUT_DELAY : inputDelay;
    }

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {

		ScriptUtils.PageResult result;
	    byte[] screenShot;
	    String finalUrl;
	    try {
	        result = load();
	        if (captcha()) {
	            closeDriver(driver);
	            throw new Captcha_RobotScriptExecutionException("Обнаружена captcha");
	        }
	
	        screenShot = ScriptUtils.getScreenshot(driver);
	        finalUrl = driver.getCurrentUrl();
	    }
	    finally {
	        closeDriver(driver);
	    }
	
	    String url = ScriptUtils.getCheckUnitValue(checkUnit);
	
	    WebDriver driverEtalon = null;
	    ScriptUtils.PageResult pageResultEtalon;
	    byte[] screenShotEtalon;
	    try {
	        driverEtalon = createEtalonDriver();
	        pageResultEtalon = loadPage(url, driverEtalon, 1);
	        screenShotEtalon = ScriptUtils.getScreenshot(driverEtalon);
	    }
	    finally {
	        closeDriver(driverEtalon);
	    }
	
	    ExecutionVpnJobResult message = new ExecutionVpnJobResult();
	
	    message.setStubUrl(stubUrl);
	    message.setResponseError(result.errorCodeChrome != null);
	    message.setChromeErrorCode(result.errorCodeChrome);
	    message.setPageContent(result.pageSource);
	    message.setScreenshot(screenShot);
	    if (result.errorCodeChrome == null) {
	        message.setFinalUrlPage(finalUrl);
	    }
	
	    message.setChromeErrorCodeEtalon(pageResultEtalon.errorCodeChrome);
	    message.setPageContentEtalon(pageResultEtalon.pageSource);
	    message.setEtalonScreenshot(screenShotEtalon);
	
	    return message;
    }

    private ScriptUtils.PageResult load() {
        try {
            ScriptUtils.waitPageLoading(driver);
            return ScriptUtils.getPageSource(driver);
        } catch (TimeoutException te){
            return new ScriptUtils.PageResult(
                    null,
                    TIME_OUT_ERROR);
        }
    }

    public long getInputDelay() {
        return inputDelay;
    }

    protected abstract boolean captcha();
}
