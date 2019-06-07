package scripts.impl;

import java.net.MalformedURLException;
import java.util.Map;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

@Slf4j
public abstract class AnonymizerScript extends VPNScript {

    private static final long DEFAULT_INPUT_DELAY = 300;

    private long inputDelay;

    public AnonymizerScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, long inputDelay)
    		throws MalformedURLException {
    	super(driverParams, scriptParams);
        this.inputDelay = inputDelay <= 0 ?
                DEFAULT_INPUT_DELAY : inputDelay;
    }

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        ScriptUtils.PageResult result = load();
        if (captcha())
            throw new Captcha_RobotScriptExecutionException("Обнаружена captcha");

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        message.setStubUrl(stubUrl);
        message.setResponseError(result.errorCodeChrome != null);
        message.setChromeErrorCode(result.errorCodeChrome);

        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        if (result.errorCodeChrome == null) {
            message.setPageContent(result.pageSource);
            message.setFinalUrlPage(driver.getCurrentUrl());
        }

        WebDriver etalonDriver = createEtalonDriver();
        String url = ScriptUtils.getCheckUnitValue(checkUnit);
        etalonDriver.get(url);
        etalonDriver.manage().window().fullscreen();
        ScriptUtils.PageResult etalon = new ScriptUtils.PageResult();
        try {
            ScriptUtils.waitPageLoading(etalonDriver);
            etalon = ScriptUtils.getPageSource(etalonDriver);
            message.setChromeErrorCodeEtalon(etalon.errorCodeChrome);
            message.setPageContentEtalon(etalon.pageSource);
        }
        catch (TimeoutException e) {
            log.error("Ошибка при получении эталона", e);
            etalon.errorCodeChrome = TIME_OUT_ERROR;
        }
        message.setEtalonScreenshot(ScriptUtils.getScreenshot(etalonDriver));
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
