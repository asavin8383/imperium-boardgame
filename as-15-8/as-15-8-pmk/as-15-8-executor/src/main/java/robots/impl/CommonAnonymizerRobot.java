package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionAnonymizerResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
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

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException {
        driver.get(anonymizerURL);

        try {
            ScriptUtils.waitPageLoading(driver);
            WebElement input = driver.findElement(By.xpath(xpathField));
            input.sendKeys(checkUnit.getValue());
            driver.findElement(By.xpath(xpathButton)).click();


            ScriptUtils.waitPageLoading(driver);

            CloudflareUtils.waitCloudflareRedirect(driver);
            ScriptUtils.waitPageLoading(driver);
            if (CloudflareUtils.isCloudflareError(driver)) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(driver));
            }

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(driver);
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
