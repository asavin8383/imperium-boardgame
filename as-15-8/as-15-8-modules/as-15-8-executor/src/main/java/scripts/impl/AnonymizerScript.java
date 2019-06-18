package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionAnonymizerResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.springframework.util.StringUtils;
import scripts.*;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutScriptException;
import scripts.utils.CloudflareUtils;
import scripts.utils.ScriptUtils;

import java.util.Map;

import static scripts.utils.ScriptUtils.TIME_OUT_ERROR;

@Slf4j
public abstract class AnonymizerScript extends SeleniumRobotScript {


    protected boolean useEtalon;
    private String etalonProxy;
    protected boolean ignoreCaptcha = false;

    protected ExecutionAnonymizerResult message;

    public AnonymizerScript(ScriptDriverParameters driverParams,
                            Map<AccessToolParameters, String> scriptParams) {

    	super(driverParams, scriptParams,
                ProxyUtils.getFullProxy(
                        scriptParams.get(AccessToolParameters.PROXY_TYPE),
                        scriptParams.get(AccessToolParameters.PROXY_DNS_NAME),
                        scriptParams.get(AccessToolParameters.PROXY_PORT),
                        scriptParams.get(AccessToolParameters.PROXY_USER),
                        scriptParams.get(AccessToolParameters.PROXY_PASSWORD)
                ));

        this.useEtalon = ScriptUtils.useEtalon(scriptParams);

        this.etalonProxy = ProxyUtils.getFullProxy(
                scriptParams.get(AccessToolParameters.ETALON_PROXY_TYPE),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
        );

        String ignoreCaptchaApps = scriptParams.get(AccessToolParameters.IGNORE_CAPTCHA_APPS);
        ignoreCaptchaApps = ignoreCaptchaApps == null ? "" : ignoreCaptchaApps;

        String appName = getDriverParams().getApplicationName();
        appName = appName == null ? "" : appName.toLowerCase();
        ignoreCaptcha = ignoreCaptchaApps.toLowerCase().contains(appName);

        this.message = new ExecutionAnonymizerResult();
        this.message.setStubUrl(scriptParams.get(AccessToolParameters.STUB_URL));
    }

    ExecutionJobResult process(CheckUnit checkUnit) throws RobotScriptExecutionException {

        ScriptUtils.PageResult page = ScriptUtils.getPageSource(driver);

        message.setErrorCode(page.errorCodeChrome);
        message.setPageContent(page.pageSource);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));

        if (message.hasError())
            return message;

        if (StringUtils.isEmpty(message.getFinalUrl())){
            message.setFinalUrl(driver.getCurrentUrl());
        }

        close(driver);

        if (useEtalon){
            driver = DriverFactory.createDriver(
                    getDriverParams().getHubURL(),
                    getDriverParams().getPlatformName(),
                    getDriverParams().getApplicationName(),
                    getDriverParams().getBrowserName(),
                    etalonProxy);

            driver.manage().window().fullscreen();
            driver.get(ScriptUtils.getCheckUnitValue(checkUnit));
            ScriptUtils.PageResult etalon = loadEtalon();

            message.setEtalonErrorCode(etalon.errorCodeChrome);
            message.setEtalonPageContent(etalon.pageSource);
            message.setEtalonScreenshot(ScriptUtils.getScreenshot(driver));

            close(driver);
        }

        message.setUseEtalon(this.useEtalon);

	    return message;
    }

    ExecutionJobResult getTimeoutMessage() {
        message.setErrorCode(TIME_OUT_ERROR);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }

    ExecutionJobResult getErrorMessage(String details) {
        message.setErrorCode(details);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }

    private ScriptUtils.PageResult loadEtalon() throws RobotScriptExecutionException {
        try {
            ScriptUtils.waitPageLoading(driver);
            CloudflareUtils.waitCloudflareRedirect(driver);
            ScriptUtils.waitPageLoading(driver);
            if (CloudflareUtils.isCloudflareError(driver)) {
                new ScriptUtils.PageResult(null,
                        CloudflareUtils.getCloudflareErrorDetails(driver));
            }
            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(driver);
            if (plainError != null)
                return new ScriptUtils.PageResult(null, plainError);

            return ScriptUtils.getPageSource(driver);
        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении эталона", e);
            return new ScriptUtils.PageResult(null, TIME_OUT_ERROR);
        } catch (InterruptedException e) {
            throw new RobotScriptExecutionException(
                    "Выполнение потока прервано", e);
        }
    }

}
