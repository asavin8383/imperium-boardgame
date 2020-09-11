package robots.impl;

import static enums.CheckUnitJobResult.INTERNAL_ERROR;
import static robots.utils.ScriptUtils.TIME_OUT_CHECKING_ERROR;
import static robots.utils.ScriptUtils.getTextOrDefault;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import enums.AccessToolParameter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import checkUnits.CheckUnit;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import robots.exceptions.ExecutionException;
import robots.exceptions.TimeoutCheckingBrowserException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.CloudflareUtils;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;

@Slf4j
public class HideMyAssRobot extends AnonymizerRobot {
	
	private static final String URL = "https://proxy.hidemyass.com";

    public static Integer WAIT_TIMEOUT_PAGE = 60;
    public static Integer WAIT_TIMEOUT = 30;

	public static final String HIDEMYASS_RETRY_AGREE_DETECTED   = "HIDEMYASS_RETRY_AGREE_DETECTED";
    public static final String HIDEMYASS_CAPTCHA                = "HIDEMYASS_CAPTCHA";
	public static final String HIDEMYASS_ERROR                  = INTERNAL_ERROR.name() + "__HIDEMYASS_ERROR";

    private static final int DEFAULT_LOAD_TIMEOUT = 5;      // seconds
    private static final String AGREE_BUTTON_TEXT = "Agree & Connect".toLowerCase();


    public HideMyAssRobot(Map<AccessToolParameter, String> scriptParams) {
		super(scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException, InterruptedException {

        getDriver().manage().timeouts().pageLoadTimeout(WAIT_TIMEOUT_PAGE, TimeUnit.SECONDS);

        try {
            ScriptUtils.PageResult pageResult = null;
            try {
                pageResult = RobotScriptUtils.simpleLoadPage(getDriver(), URL, WAIT_TIMEOUT, 3);
            }
            catch (TimeoutException e){
                return getErrorMessage(HIDEMYASS_ERROR, "Таймаут. Hidemyass недоступен!");
            }
            if (pageResult.errorCodeChrome != null){
                return getErrorMessage(HIDEMYASS_ERROR, "Hidemyass недоступен! " + pageResult.errorCodeChrome + ".");
            }

            WebElement input = getDriver().findElement(By.id("form_url_fake"));
            input.sendKeys(checkUnit.getValue());

            By submitLocator = By.xpath("/html/body/form/div[3]/a");
            WebElement submitButton = ScriptUtils.getElementBy(submitLocator, getDriver());
            if (submitButton == null || !isSubmitButton(submitButton)) {
                return getErrorMessage(HIDEMYASS_ERROR, "Не удалось найти кнопку перехода.");
            }

            submitButton.click();

            ScriptUtils.waitDriver(getDriver(), 1);
            ScriptUtils.waitPageLoading(getDriver(), WAIT_TIMEOUT);

            CloudflareUtils.waitCloudflareRedirect(getDriver(), WAIT_TIMEOUT, true);

            ScriptUtils.waitPageLoading(getDriver(), WAIT_TIMEOUT);

            if (CloudflareUtils.isCloudflareError(getDriver())) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(getDriver()));
            }

            if (captcha()) {
                log.info("Обнаружена captcha-form на HideMyAss");
                return getErrorMessage(HIDEMYASS_CAPTCHA);
               /* if (false && !this.ignoreCaptcha) {
                    throw new Captcha_ExecutionException("Обнаружена captcha-form на HideMyAss");
                }*/
            }

            if (isHideMyAssErrorPage())
                return getErrorMessage(getHideMyAssErrorDetails());

            WebElement agreeButton = getAgreeButtonPage();
            if (agreeButton != null){
                log.info("Обнаружена страница 'Agree & Connect', попытка пройти");
                agreeButton.click();
                ScriptUtils.waitDriver(getDriver(), 5);
                ScriptUtils.waitPageLoading(getDriver());
            }
            WebElement agreeButtonSecond = getAgreeButtonPage();
            if (agreeButtonSecond != null){
                log.info("Повторно обнаружена страница 'Agree & Connect'.");
                return getErrorMessage(HIDEMYASS_RETRY_AGREE_DETECTED, "Неудачный повторый переход по кнопке 'Agree & Connect'.");
            }

            message.setFinalUrl(getFinalUrl());

            ScriptUtils.tryRemoveElementById(getDriver(), "hma-top");

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(getDriver());
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
            return getErrorMessage(HIDEMYASS_ERROR, "Не удалось найти элементы навигации.");
        }
    }

    private boolean captcha() {
        try {
            WebElement captchaForm = getDriver().findElement(
                    By.xpath("//*[@id=\"captcha-form\"]"));
            return captchaForm != null;
        } catch (NoSuchElementException | InterruptedException e) {
            // ignore
        }
        return false;
    }

    private boolean isHideMyAssErrorPage() throws InterruptedException {
        WebElement headerElement = ScriptUtils.findElementIfExists(
                By.xpath("//*[@id=\"top\"]/div/h1"), getDriver());
        String text = ScriptUtils.getTextOrDefault(
                headerElement, null);
        return text != null && text.equalsIgnoreCase(
                "Oops! Something’s gone wrong...");
    }

    private String getHideMyAssErrorDetails() throws InterruptedException {
        WebElement element = ScriptUtils.findElementIfExists(
                By.xpath("//*[@id=\"top\"]/div/p"), getDriver());
        return ScriptUtils.getTextOrDefault(element,
                "Описание ошибки не найдено");
    }

    private WebElement getAgreeButtonPage() throws InterruptedException {
        WebElement button = ScriptUtils.findElementIfExists(
                By.cssSelector(".terms-agree-wrapper > a.button"), getDriver());
        String text = ScriptUtils.getTextOrDefault(
                button, null);
        return text != null &&
                text.toLowerCase().contains("Agree & Connect".toLowerCase()) ? button : null;
    }

    private String getFinalUrl() throws InterruptedException {
        WebElement button = ScriptUtils.findElementIfExists(
                By.cssSelector("input#hma-top-input-url"), getDriver());
        String text = button == null ? null : button.getAttribute("value");
        return text == null ? null : text.trim();
    }

    /**
     * Метод, который скорее всего пригодится.
     */
    @SuppressWarnings("unused")
	private boolean submit(WebDriver driver, By locator, int retryCount) throws InterruptedException {
        WebElement button = ScriptUtils.waitClickable(getDriver(), locator, DEFAULT_LOAD_TIMEOUT);

        Consumer<WebElement> tryClick = el -> { if (el != null) el.click(); };

        Supplier<WebElement> refresher = () -> {
            WebElement el = null;
            try {
                el = ScriptUtils.findElementIfExists(locator, getDriver());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                ScriptUtils.waitDriver(getDriver(), 1);
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
