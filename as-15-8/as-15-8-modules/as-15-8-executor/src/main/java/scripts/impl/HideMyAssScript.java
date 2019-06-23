package scripts.impl;

import static enums.CheckUnitJobResult.INTERNAL_ERROR;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;
import static scripts.utils.ScriptUtils.TIME_OUT_CHECKING_ERROR;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.ui.WebDriverWait;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutCheckingBrowserException;
import scripts.exceptions.TimeoutScriptException;
import scripts.utils.CloudflareUtils;
import scripts.utils.RobotScriptUtils;
import scripts.utils.ScriptUtils;

@Slf4j
public class HideMyAssScript extends AnonymizerScript {
	
	private static final String URL = "https://proxy.hidemyass.com";

    public static Integer WAIT_TIMEOUT = 60;

	public static final String HIDEMYASS_RETRY_AGREE_DETECTED   = "HIDEMYASS_RETRY_AGREE_DETECTED";
	public static final String HIDEMYASS_TIMEOUT                = INTERNAL_ERROR.name() + "__TIMEOUT";
	public static final String HIDEMYASS_NOT_FOUND_ELEMENT      = INTERNAL_ERROR.name() + "__NOT_FOUND_ELEMENT";


    public HideMyAssScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		super(driverParams, scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {

        driver.manage().timeouts().pageLoadTimeout(WAIT_TIMEOUT, TimeUnit.SECONDS);

        try {
            try{
                RobotScriptUtils.simpleLoadPage(driver, URL, WAIT_TIMEOUT, 2);
            }
            catch (TimeoutException e){
                return getErrorMessageDetails(HIDEMYASS_TIMEOUT, "Таймаут. Hidemyass недоступен!");
            }

            WebElement input = driver.findElement(By.id("form_url_fake"));
            input.sendKeys(checkUnit.getValue());

            WebElement submitButton = getSubmitButton();
            if (submitButton == null) {
                return getErrorMessageDetails(HIDEMYASS_NOT_FOUND_ELEMENT, "Не удалось найти кнопку перехода.");
            }

            submitButton.click();
            ScriptUtils.waitDriver(driver, 1);
            ScriptUtils.waitPageLoading(driver);

            CloudflareUtils.waitCloudflareRedirect(driver);
            ScriptUtils.waitPageLoading(driver);
            if (CloudflareUtils.isCloudflareError(driver)) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(driver));
            }

            if (captcha()) {
                log.info("Обнаружена captcha-form на HideMyAss");
               /* if (false && !this.ignoreCaptcha) {
                    throw new Captcha_RobotScriptExecutionException("Обнаружена captcha-form на HideMyAss");
                }*/
            }

            if (isHideMyAssErrorPage())
                return getErrorMessage(getHideMyAssErrorDetails());

            WebElement agreeButton = getAgreeButtonPage();
            if (agreeButton != null){
                log.info("Обнаружена страница 'Agree & Connect', попытка пройти");
                agreeButton.click();
                ScriptUtils.waitDriver(driver, 5);
                ScriptUtils.waitPageLoading(driver);
            }
            WebElement agreeButtonSecond = getAgreeButtonPage();
            if (agreeButtonSecond != null){
                log.info("Повторно обнаружена страница 'Agree & Connect'.");
                return getErrorMessageDetails(HIDEMYASS_RETRY_AGREE_DETECTED, "Неудачный повторый переход по кнопке 'Agree & Connect'.");
            }

            message.setFinalUrl(getFinalUrl());

            ScriptUtils.tryRemoveElementById(driver, "hma-top");

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(driver);
            if (plainError != null)
                return getErrorMessage(plainError);

            return process(checkUnit);

        } catch (TimeoutCheckingBrowserException e) {
            log.info("TimeoutException (checking browser) при получении страницы", e);
            return getErrorMessage(TIME_OUT_CHECKING_ERROR);
        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении страницы", e);
            return getTimeoutMessage();
        } catch (NoSuchElementException e) {
            return getErrorMessageDetails(HIDEMYASS_NOT_FOUND_ELEMENT, "Не удалось найти элементы навигации");
        } catch (InterruptedException e) {
            throw new RobotScriptExecutionException("Выполнение потока прервано", e);
        }
    }

    private boolean captcha() {
        try {
            WebElement captchaForm = driver.findElement(
                    By.xpath("//*[@id=\"captcha-form\"]"));
            return captchaForm != null;
        } catch (NoSuchElementException e) {
            // ignore
        }
        return false;
    }

    private boolean isHideMyAssErrorPage() {
        WebElement headerElement = ScriptUtils.findElementIfExists(
                By.xpath("//*[@id=\"top\"]/div/h1"), driver);
        String text = ScriptUtils.getTextOrDefault(
                headerElement, null);
        return text != null && text.equalsIgnoreCase(
                "Oops! Something’s gone wrong...");
    }

    private String getHideMyAssErrorDetails() {
        WebElement element = ScriptUtils.findElementIfExists(
                By.xpath("//*[@id=\"top\"]/div/p"), driver);
        return ScriptUtils.getTextOrDefault(element,
                "Описание ошибки не найдено");
    }

    private WebElement getSubmitButton() {
        WebElement button = ScriptUtils.findElementIfExists(
                By.xpath("/html/body/form/div[3]/a"), driver);
        String text = ScriptUtils.getTextOrDefault(
                button, null);
        return text != null &&
                text.toLowerCase().contains("Agree & Connect".toLowerCase()) ? button : null;
    }

    private WebElement getAgreeButtonPage() {
        WebElement button = ScriptUtils.findElementIfExists(
                By.cssSelector(".terms-agree-wrapper > a.button"), driver);
        String text = ScriptUtils.getTextOrDefault(
                button, null);
        return text != null &&
                text.toLowerCase().contains("Agree & Connect".toLowerCase()) ? button : null;
    }

    private String getFinalUrl() {
        WebElement button = ScriptUtils.findElementIfExists(
                By.cssSelector("input#hma-top-input-url"), driver);
        String text = button == null ? null : button.getAttribute("value");
        return text == null ? null : text.trim();
    }
}
