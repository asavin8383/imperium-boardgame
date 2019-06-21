package scripts.impl;

import static enums.CheckUnitJobResult.INTERNAL_ERROR;
import static scripts.utils.ScriptUtils.TIME_OUT_CHECKING_ERROR;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import scripts.ScriptDriverParameters;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutCheckingBrowserException;
import scripts.exceptions.TimeoutScriptException;
import scripts.utils.CloudflareUtils;
import scripts.utils.ScriptUtils;

@Slf4j
public class HideMyAssScript extends AnonymizerScript {
	
	private static final String URL = "https://www.hidemyass.com/proxy";

	public static final String HIDEMYASS_RETRY_AGREE_DETECTED   = "HIDEMYASS_RETRY_AGREE_DETECTED";
	public static final String HIDEMYASS_NOT_FOUND_BUTTON       = INTERNAL_ERROR.name() + "__NOT_FOUND_BUTTON";
	public static final String HIDEMYASS_NOT_OPENED_URL         = INTERNAL_ERROR.name() + "__NOT_OPENED_URL";
	public static final String HIDEMYASS_NOT_FOUND_INPUT        = INTERNAL_ERROR.name() + "__NOT_FOUND_INPUT";


    public HideMyAssScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		super(driverParams, scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException { 	 
        driver.get(URL);
        driver.manage().window().fullscreen();

        try {
            ScriptUtils.waitPageLoading(driver);
            driver.switchTo().frame(driver.findElement(By.id("proxyIframe")));
            WebElement input = driver.findElement(By.id("form_url_fake"));
            input.sendKeys(checkUnit.getValue());

            WebElement submitButton = getSubmitButton();
            if (submitButton == null) {
                return getErrorMessage(HIDEMYASS_NOT_FOUND_BUTTON);     // Не удалось найти кнопку перехода!
            }

            // повторяем поиск кнопки перехода несколько раз (бывали случае когда не срабатывала с первого раза)
            int i = 0, cnt = 3;
            while (submitButton != null && i < cnt) {
                submitButton.click();
                ScriptUtils.waitPageLoading(driver);
                ScriptUtils.waitDriver(driver, 1);
                submitButton = getSubmitButton();
                i++;
            }
            if (submitButton != null) {
                return getErrorMessage(HIDEMYASS_NOT_OPENED_URL);   // Не удалось осуществить переход по кнопке!
            }

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
                return getErrorMessage(HIDEMYASS_RETRY_AGREE_DETECTED);
            }

            message.setFinalUrl(getFinalUrl());

            ScriptUtils.tryRemoveElementById(driver, "hma-top");

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(driver);
            if (plainError != null)
                return getErrorMessage(plainError);

            return process(checkUnit);

        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении страницы", e);
            if (e instanceof TimeoutCheckingBrowserException)
                return getErrorMessage(TIME_OUT_CHECKING_ERROR);
            return getTimeoutMessage();
        } catch (NoSuchElementException e) {
            return getErrorMessage(HIDEMYASS_NOT_FOUND_INPUT);  // "Не удалось найти элементы навигации HideMyAss"
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
