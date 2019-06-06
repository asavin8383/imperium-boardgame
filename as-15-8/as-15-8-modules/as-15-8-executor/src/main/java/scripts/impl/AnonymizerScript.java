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

        ScriptUtils.PageResult result;
        byte[] screenShot;
        String finalUrl;
        try {
            createDriver(vpnProxy);
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

        String url = ScriptUtils.getCheckUnitValue(getCheckUnit());

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
        fillExecutionResultMessage(message);

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

        sendExecutionResult(message);
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
