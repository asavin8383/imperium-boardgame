package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionAnonymizerResult;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;
import robots.ChromeSettings;
import robots.DriverFactory;
import robots.ProxyUtils;
import robots.exceptions.ExecutionException;
import robots.utils.CloudflareUtils;
import robots.utils.HttpResponseHelper;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;
import robots.utils.ScriptUtils.PageResult;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static robots.utils.HttpResponseHelper.HttpResponseMeta;
import static robots.utils.ScriptUtils.TIME_OUT_ERROR;


public class ExtensionRobot extends SeleniumRobot {

    private String stubUrl;

    private String xpathField;
    private String xpathButton;

    private ChromeSettings.Extension extension;
    protected ExecutionVpnJobResult message = new ExecutionVpnJobResult();

    public ExtensionRobot(Map<AccessToolParameter, String> scriptParams) {

    	super(scriptParams);

     	this.stubUrl = scriptParams.get(AccessToolParameter.STUB_URL);

     	this.extension = new ChromeSettings.Extension(
     	        scriptParams.get(AccessToolParameter.EXTENSION_ID),
                scriptParams.get(AccessToolParameter.EXTENSION_VERSION),
                scriptParams.get(AccessToolParameter.EXTENSION_POPUP)
        );

        this.xpathField = scriptParams.get(AccessToolParameter.EXTENSION_XPATH_FIELD);
        this.xpathButton = scriptParams.get(AccessToolParameter.EXTENSION_XPATH_BUTTON);

    }

    ExecutionJobResult process(CheckUnit checkUnit) throws ExecutionException {

        driver = DriverFactory.createChromeDriver(
                ExecutorProperties.getSeleniumHubUrl(),
                Platform.valueOf(getScriptParams().get(AccessToolParameter.PLATFORM)),
                getScriptParams().get(AccessToolParameter.VERSION),
                Collections.singletonList(extension)
        );

        ScriptUtils.PageResult page = ScriptUtils.getPageSource(driver);

        if (message.getHttpStatus() == null){
            HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver);
            if (responseMeta != null){
                message.setHttpStatus(responseMeta.status);
                message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
            }
        }
        message.setChromeErrorCode(page.errorCodeChrome);
        message.setPageContent(page.pageSource);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));

        if (message.hasError())
            return message;

        if (StringUtils.isEmpty(message.getFinalUrlPage())){
            message.setFinalUrlPage(ScriptUtils.getCurrentUrl(driver));
        }

        close(driver);

        return message;
    }


    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException {
        // работает только с хромом!
        if (!checkBrowserChrome())
            throw new ExecutionException("Ошибка, неверный браузер! Для данного робота поддерживатся только браузер CHROME!");

            driver.get(extension.getPopupUrl()); //gkojfkhlekighikafcpjkiklfbnlmeio/js/popup.html

            try {
                ScriptUtils.waitPageLoading(driver);
                WebElement input = driver.findElement(By.xpath(xpathField)); //"//*[@id=\"popup\"]/div/div[1]/input"
                input.sendKeys(checkUnit.getValue());
                driver.findElement(By.xpath(xpathButton)).click(); //"//*[@id=\"popup\"]/div/div[1]/button"

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
            } catch (Exception e) {


            } finally {
            close(driver);
        }

        return message;
    }

    private boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getScriptParams().get(AccessToolParameter.BROWSER));
    }

    ExecutionJobResult getTimeoutMessage() {
        message.setChromeErrorCode(TIME_OUT_ERROR);
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
        message.setChromeErrorCode(errorCode);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }

}
