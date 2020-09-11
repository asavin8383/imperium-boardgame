package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionAnonymizerResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.util.StringUtils;
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
public class CommonAnonymizerRobot extends SeleniumRobot {

    private String anonymizerURL;
    private String xpathField;
    private String xpathButton;

    protected ExecutionAnonymizerResult message;

    public CommonAnonymizerRobot(Map<AccessToolParameter, String> scriptParams) {

    	super(scriptParams,
                ProxyUtils.getFullProxy(
                        scriptParams.get(AccessToolParameter.PROXY_TYPE),
                        scriptParams.get(AccessToolParameter.PROXY_DNS_NAME),
                        scriptParams.get(AccessToolParameter.PROXY_PORT)
                ));

        this.message = new ExecutionAnonymizerResult();
        this.message.setStubUrl(scriptParams.get(AccessToolParameter.STUB_URL));
        this.message.setUseEtalon(ExecutorProperties.getEtalon().getEnabled());
        this.anonymizerURL = scriptParams.get(AccessToolParameter.ANONYMIZER_URL);
        this.xpathField = scriptParams.get(AccessToolParameter.ANONYMIZER_XPATH_FIELD);
        this.xpathButton = scriptParams.get(AccessToolParameter.ANONYMIZER_XPATH_BUTTON);
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

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException, InterruptedException {
        getDriver().get(anonymizerURL);

        try {
            ScriptUtils.waitPageLoading(getDriver());
            WebElement input = getDriver().findElement(By.xpath(xpathField));
            input.sendKeys(checkUnit.getValue());
            getDriver().findElement(By.xpath(xpathButton)).click();


            ScriptUtils.waitPageLoading(getDriver());

            CloudflareUtils.waitCloudflareRedirect(getDriver());
            ScriptUtils.waitPageLoading(getDriver());
            if (CloudflareUtils.isCloudflareError(getDriver())) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(getDriver()));
            }

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(getDriver());
            if (plainError != null)
                return getErrorMessage(plainError);

            return process(checkUnit);

        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении страницы", e);
            return getTimeoutMessage();
        } catch (NoSuchElementException e) {
            throw new ExecutionException(
                    "Не удалось найти элементы навигации CommonAnonymizer", e);
        } catch (InterruptedException e) {
            throw new ExecutionException(
                    "Выполнение потока прервано", e);
        }
    }
}
