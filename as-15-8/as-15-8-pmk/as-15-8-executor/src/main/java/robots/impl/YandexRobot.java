package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameter;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Slf4j(topic = "robots")
public class YandexRobot extends CommonDirectSearchRobot {

    public YandexRobot(Map<AccessToolParameter, String> scriptParams) {
        super(scriptParams);
    }

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException {
        driver.get(getSearchSystemUrl());

        equalityTest = EqualityTest.forCheckUnit(checkUnit);
        if (checkSuggestedLink(checkUnit.getValue(), equalityTest))
            return createMessage(true, null);

        if (!solveCaptcha(this.driver))
            if (throwExceptionByCaptchaOrBadIP) {
                throw new Captcha_ExecutionException(String.format("ПС выдала капчу на url: %s", checkUnit.getValue()));
            } else {
                return createMessage(true, CheckUnitJobResult.CAPTCHA_DETECTED);
            }

        return createMessage(checkPaginatedSearchResult(), null);
    }

    String extractUrl(WebElement element) {
        String url = element.getAttribute("href");
        return isWrapped(url) ? getDirectUrl(url) : url;
    }



    private boolean checkLink(@Nullable WebElement element, EqualityTest test) {
        if (element == null)
            return false;

        String url = extractUrl(element);
        try {
            return test.equalTo(url);

        } catch (TimeoutException | MalformedURLException e) {
            log.warn("Ошибка при проверке ссылки '{}' Yandex: {}",
                    url, e.getLocalizedMessage());
        }
        return false;
    }

    private boolean checkSuggestedLink(String query, EqualityTest test) {
        WebElement inputField = driver.findElement(By.xpath(getXpathInputField()));
        ScriptUtils.type(inputField, getInputDelay(), query + " ");

        WebElement suggestedLink = getSuggestedLink(driver);
        if (checkLink(suggestedLink, test)) return true;

        inputField.sendKeys(Keys.ENTER);
        return false;
    }

    @Nullable
    private WebElement getSuggestedLink(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver,
                    1);
            return wait.until(presenceOfElementLocated(
                    By.className("suggest2-item__link")));
        } catch (TimeoutException | NoSuchElementException ignore) {
            return null;
        }
    }

    private String getDirectUrl(String wrappedUrl) {
        // open new tab
        ((JavascriptExecutor) driver).executeScript("window.open()");
        // switch to tab
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        // navigate to url
        driver.get(wrappedUrl);
        // get current url
        String result = driver.getCurrentUrl();
        // close tab
        driver.close();
        // switch to previous tab
        driver.switchTo().window(tabs.get(0));
        return result;
    }

    private static boolean isWrapped(String url) {
        return url.contains("yandex.ru/count/");
    }
}
