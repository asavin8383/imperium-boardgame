package scripts.impl;

import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.springframework.util.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import scripts.ScriptUtils;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

@Slf4j
public abstract class AnonymizerScript extends VPNScript {

    private static final long DEFAULT_INPUT_DELAY = 300;

    private long inputDelay;

    @BeforeClass
    @Parameters({"inputDelay"})
    public void setOptions(String inputDelay) {
        this.inputDelay = StringUtils.isEmpty(inputDelay) ?
                DEFAULT_INPUT_DELAY : Long.parseLong(inputDelay);
    }

    @Override
    public void execute() throws RobotScriptExecutionException {
        ScriptUtils.PageResult result = load();
        if (captcha())
            throw new Captcha_RobotScriptExecutionException("Обнаружена captcha");

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        fillExecutionResultMessage(message);
        message.setStubUrl(stubUrl);
        message.setResponseError(result.errorCodeChrome != null);
        message.setChromeErrorCode(result.errorCodeChrome);

        if (result.errorCodeChrome == null) {
            message.setPageContent(result.pageSource);
            message.setScreenshot(ScriptUtils.getScreenshot(driver));
            message.setFinalUrlPage(driver.getCurrentUrl());
        }

        WebDriver etalonDriver = createEtalonDriver();
        etalonDriver.get(getCheckUnit().getValue());
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
            etalon.errorCodeChrome = "TIME_OUT";
        }
        if (etalon.errorCodeChrome == null){
            message.setEtalonScreenshot(ScriptUtils.getScreenshot(etalonDriver));
        }
        sendExecutionResult(message);
    }

    private ScriptUtils.PageResult load() {
        try {
            ScriptUtils.waitPageLoading(driver);
            return ScriptUtils.getPageSource(driver);
        } catch (TimeoutException te){
            return new ScriptUtils.PageResult(
                    null,
                    "TIME_OUT");
        }
    }

    public long getInputDelay() {
        return inputDelay;
    }

    protected abstract boolean captcha();
}
