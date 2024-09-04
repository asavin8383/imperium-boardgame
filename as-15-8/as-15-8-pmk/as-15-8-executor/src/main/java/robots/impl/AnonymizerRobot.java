package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionAnonymizerResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.springframework.util.StringUtils;
import robots.DriverFactory;
import robots.ProxyUtils;
import robots.exceptions.ExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.CloudflareUtils;
import robots.utils.HttpResponseHelper;
import robots.utils.HttpResponseHelper.HttpResponseMeta;
import robots.utils.ScriptUtils;

import java.util.Map;

import static robots.utils.ScriptUtils.TIME_OUT_ERROR;

@Slf4j
public abstract class AnonymizerRobot extends SeleniumRobot {


    protected boolean useEtalon;
    private String etalonProxy;
    protected boolean ignoreCaptcha;

    protected ExecutionAnonymizerResult message;

    public AnonymizerRobot(Map<AccessToolParameter, String> scriptParams) {

    	super(scriptParams,
                ProxyUtils.getFullProxy(
                        scriptParams.get(AccessToolParameter.PROXY_TYPE),
                        scriptParams.get(AccessToolParameter.PROXY_DNS_NAME),
                        scriptParams.get(AccessToolParameter.PROXY_PORT)
                ));

        ExecutorProperties.EtalonProperties etalonProperties = ExecutorProperties.getEtalon();
        this.useEtalon = etalonProperties.getEnabled();

        this.etalonProxy = ProxyUtils.getFullProxy(
                etalonProperties.getProxy().getType(),
                etalonProperties.getProxy().getHost(),
                etalonProperties.getProxy().getPort()
        );

        String ignoreCaptchaApps = scriptParams.get(AccessToolParameter.IGNORE_CAPTCHA_APPS);
        ignoreCaptchaApps = ignoreCaptchaApps == null ? "" : ignoreCaptchaApps;

        String appName = getScriptParams().get(AccessToolParameter.VERSION);
        appName = appName == null ? "" : appName.toLowerCase();
        ignoreCaptcha = ignoreCaptchaApps.toLowerCase().contains(appName);

        this.message = new ExecutionAnonymizerResult();
        this.message.setStubUrl(scriptParams.get(AccessToolParameter.STUB_URL));
    }

    ExecutionJobResult process(CheckUnit checkUnit) throws ExecutionException {

        ScriptUtils.PageResult page = ScriptUtils.getPageSource(driver);

        if (message.getHttpStatus() == null){
            HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver);
            if (responseMeta != null){
                message.setHttpStatus(responseMeta.status);
                message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
            }
        }
        message.setErrorCode(page.errorCodeChrome);
        message.setPageContent(page.pageSource);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));

        if (message.hasError())
            return message;

        if (StringUtils.isEmpty(message.getFinalUrl())){
            message.setFinalUrl(ScriptUtils.getCurrentUrl(driver));
        }

        close(driver);

        if (useEtalon){
            driver = DriverFactory.createDriver(
                    ExecutorProperties.getSeleniumHubUrl(),
                    Platform.valueOf(getScriptParams().get(AccessToolParameter.PLATFORM)),
                    getScriptParams().get(AccessToolParameter.BROWSER),
                    getScriptParams().get(AccessToolParameter.VERSION),
                    etalonProxy,
                    true);

            driver.get(ScriptUtils.getCheckUnitValue(checkUnit));
            ScriptUtils.PageResult etalon = loadEtalon();
            HttpResponseMeta responseMetaEtalon = HttpResponseHelper.getGetResponseMeta(driver);

            if (responseMetaEtalon != null){
                message.setHttpStatusEtalon(responseMetaEtalon.status);
                message.setHttpHeadersEtalon(HttpResponseHelper.headers2Str(responseMetaEtalon.jsonHeaders));
            }
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

    ExecutionJobResult getErrorMessage(String errorCode) {
        return getErrorMessage(errorCode, null);
    }

    ExecutionJobResult getErrorMessage(String errorCode, String details) {
        HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver);
        if (responseMeta != null){
            message.setHttpStatus(responseMeta.status);
            message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
        }
        message.setErrorCode(errorCode);
        if (details != null)
            message.setDetails(details);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }

    private ScriptUtils.PageResult loadEtalon() throws ExecutionException {
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
            throw new ExecutionException(
                    "Выполнение потока прервано", e);
        }
    }

}
