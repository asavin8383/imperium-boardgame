package scripts.impl;

import static enums.CheckUnitJobResult.INTERNAL_ERROR;
import static scripts.utils.ScriptUtils.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.openqa.selenium.*;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
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
    public static final String HIDEMYASS_CAPTCHA                = "HIDEMYASS_CAPTCHA";
	public static final String HIDEMYASS_ERROR                  = INTERNAL_ERROR.name() + "__HIDEMYASS_ERROR";

    private static final int DEFAULT_LOAD_TIMEOUT = 5;      // seconds
    private static final String AGREE_BUTTON_TEXT = "Agree & Connect".toLowerCase();


    public HideMyAssScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		super(driverParams, scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {

        driver.manage().timeouts().pageLoadTimeout(WAIT_TIMEOUT, TimeUnit.SECONDS);

        try {
            ScriptUtils.PageResult pageResult = null;
            try {
                pageResult = RobotScriptUtils.simpleLoadPage(driver, URL, WAIT_TIMEOUT, 3);
            }
            catch (TimeoutException e){
                return getErrorMessageDetails(HIDEMYASS_ERROR, "Таймаут. Hidemyass недоступен!");
            }
            if (pageResult.errorCodeChrome != null){
                return getErrorMessageDetails(HIDEMYASS_ERROR, "Hidemyass недоступен! " + pageResult.errorCodeChrome + ".");
            }

            WebElement input = driver.findElement(By.id("form_url_fake"));
            input.sendKeys(checkUnit.getValue());

            By submitLocator = By.xpath("/html/body/form/div[3]/a");
            WebElement submitButton = ScriptUtils.getElementBy(submitLocator, driver);
            if (submitButton == null || !isSubmitButton(submitButton)) {
                return getErrorMessageDetails(HIDEMYASS_ERROR, "Не удалось найти кнопку перехода.");
            }

            submitButton.click();

            ScriptUtils.waitDriver(driver, 1);
            ScriptUtils.waitPageLoading(driver, WAIT_TIMEOUT);

            CloudflareUtils.waitCloudflareRedirect(driver, WAIT_TIMEOUT, true);

            ScriptUtils.waitPageLoading(driver, WAIT_TIMEOUT);

            if (CloudflareUtils.isCloudflareError(driver)) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(driver));
            }

            if (captcha()) {
                log.info("Обнаружена captcha-form на HideMyAss");
                return getErrorMessage(HIDEMYASS_CAPTCHA);
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
            return getErrorMessageDetails(HIDEMYASS_ERROR, "Не удалось найти элементы навигации.");
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

    /**
     * Метод, который скорее всего пригодится.
     */
    private boolean submit(WebDriver driver, By locator, int retryCount) {
        WebElement button = ScriptUtils.waitClickable(driver, locator, DEFAULT_LOAD_TIMEOUT);

        Consumer<WebElement> tryClick = el -> { if (el != null) el.click(); };

        Supplier<WebElement> refresher = () -> {
            WebElement el = ScriptUtils.findElementIfExists(locator, driver);
            return isSubmitButton(el) ? el : null;
        };

        int counter = 0;

        do {
            try {
                // if null or stale on the first try ?
                button = ScriptUtils.actOnStaleRef(button,
                        tryClick, refresher);
            } catch (StaleElementReferenceException e) {
                return true;
            }
            if (button != null) {
                ScriptUtils.waitDriver(driver, 1);
                button = refresher.get();
            }
        } while (button != null && ++counter < retryCount);

        return button == null && counter < retryCount;
    }

    private boolean isSubmitButton(WebElement button) {
        String text = getTextOrDefault(button, null);
        return text != null && text.toLowerCase().contains(AGREE_BUTTON_TEXT);
    }
}
