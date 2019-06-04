package scripts.impl;

import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;

@Slf4j
public abstract class AnonymizerScript extends VPNScript {

    @Override
    public void execute() throws RobotScriptExecutionException {
        ScriptUtils.PageResult result = load();

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        fillExecutionResultMessage(message);
        message.setStubUrl(stubUrl);
        message.setResponseError(result.errorCodeChrome != null);
        message.setChromeErrorCode(result.errorCodeChrome);

        if (result.errorCodeChrome == null) {
            message.setPageContent(result.pageSource);
            message.setScreenshot(ScriptUtils.getScreenshot(driver));
            message.setFinalUrlPage(driver.getCurrentUrl());

            WebDriver etalonDriver = createEtalonDriver();
            etalonDriver.get(getCheckUnit().getValue());
            etalonDriver.manage().window().fullscreen();
            try {
                ScriptUtils.waitPageLoading(etalonDriver);
                ScriptUtils.PageResult etalon =
                        ScriptUtils.getPageSource(etalonDriver);
                message.setPageContentEtalon(etalon.pageSource);
            }
            catch (TimeoutException e) {
                log.error("Ошибка при получении эталона", e);
            }
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
}
