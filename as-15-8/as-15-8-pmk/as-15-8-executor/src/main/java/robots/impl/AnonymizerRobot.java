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
    protected boolean ignoreCaptcha = false;

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

    ExecutionJobResult process(CheckUnit checkUnit) throws ExecutionException, InterruptedException {

        ScriptUtils.PageResult page = ScriptUtils.getPageSource(getDriver());

        if (message.getHttpStatus() == null){
            HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(getDriver());
            if (responseMeta != null){
                message.setHttpStatus(responseMeta.status);
                message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
            }
        }
        message.setErrorCode(page.errorCodeChrome);
        message.setPageContent(page.pageSource);
        message.setScreenshot(ScriptUtils.getScreenshot(getDriver()));

        if (message.hasError())
            return message;

        if (StringUtils.isEmpty(message.getFinalUrl())){
            message.setFinalUrl(ScriptUtils.getCurrentUrl(getDriver()));
        }

        close(getDriver());

        if (useEtalon){
            setDriver(DriverFactory.createDriver(
                    ExecutorProperties.getSeleniumHubUrl(),
                    Platform.valueOf(getScriptParams().get(AccessToolParameter.PLATFORM)),
                    getScriptParams().get(AccessToolParameter.BROWSER),
                    getScriptParams().get(AccessToolParameter.VERSION),
                    etalonProxy,
                    true)
            );

            getDriver().get(ScriptUtils.getCheckUnitValue(checkUnit));
            ScriptUtils.PageResult etalon = loadEtalon();
            HttpResponseMeta responseMetaEtalon = HttpResponseHelper.getGetResponseMeta(getDriver());

            if (responseMetaEtalon != null){
                message.setHttpStatusEtalon(responseMetaEtalon.status);
                message.setHttpHeadersEtalon(HttpResponseHelper.headers2Str(responseMetaEtalon.jsonHeaders));
            }
            message.setEtalonErrorCode(etalon.errorCodeChrome);
            message.setEtalonPageContent(etalon.pageSource);
            message.setEtalonScreenshot(ScriptUtils.getScreenshot(getDriver()));

            close(getDriver());
        }

        message.setUseEtalon(this.useEtalon);

	    return message;
    }

    ExecutionJobResult getTimeoutMessage() throws InterruptedException {
        message.setErrorCode(TIME_OUT_ERROR);
        message.setScreenshot(ScriptUtils.getScreenshot(getDriver()));
        return message;
    }

    ExecutionJobResult getErrorMessage(String errorCode) throws InterruptedException {
        return getErrorMessage(errorCode, null);
    }

    ExecutionJobResult getErrorMessage(String errorCode, String details) throws InterruptedException {
        HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(getDriver());
        if (responseMeta != null){
            message.setHttpStatus(responseMeta.status);
            message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
        }
        message.setErrorCode(errorCode);
        if (details != null)
            message.setDetails(details);
        message.setScreenshot(ScriptUtils.getScreenshot(getDriver()));
        return message;
    }

    private ScriptUtils.PageResult loadEtalon() throws ExecutionException {
        try {
            ScriptUtils.waitPageLoading(getDriver());
            CloudflareUtils.waitCloudflareRedirect(getDriver());
            ScriptUtils.waitPageLoading(getDriver());
            if (CloudflareUtils.isCloudflareError(getDriver())) {
                new ScriptUtils.PageResult(null,
                        CloudflareUtils.getCloudflareErrorDetails(getDriver()));
            }
            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(getDriver());
            if (plainError != null)
                return new ScriptUtils.PageResult(null, plainError);

            return ScriptUtils.getPageSource(getDriver());
        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении эталона", e);
            return new ScriptUtils.PageResult(null, TIME_OUT_ERROR);
        } catch (InterruptedException e) {
            throw new ExecutionException(
                    "Выполнение потока прервано", e);
        }
    }

}
