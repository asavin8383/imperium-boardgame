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
    public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException, InterruptedException {
        getDriver().get(getSearchSystemUrl());

        equalityTest = EqualityTest.forCheckUnit(checkUnit);
        if (checkSuggestedLink(checkUnit.getValue(), equalityTest))
            return createMessage(true, null);

        if (captcha())
            return createMessage(true, CheckUnitJobResult.CAPTCHA_DETECTED);

        return createMessage(checkPaginatedSearchResult(), null);
    }

    String extractUrl(WebElement element) throws InterruptedException {
        String url = element.getAttribute("href");
        return isWrapped(url) ? getDirectUrl(url) : url;
    }



    private boolean checkLink(@Nullable WebElement element, EqualityTest test) throws InterruptedException {
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

    private boolean checkSuggestedLink(String query, EqualityTest test) throws InterruptedException {
        WebElement inputField = getDriver().findElement(By.xpath(getXpathInputField()));
        ScriptUtils.type(inputField, getInputDelay(), query + " ");

        WebElement suggestedLink = getSuggestedLink(getDriver());
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

    private String getDirectUrl(String wrappedUrl) throws InterruptedException {
        // open new tab
        ((JavascriptExecutor) getDriver()).executeScript("window.open()");
        // switch to tab
        ArrayList<String> tabs = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(tabs.get(1));
        // navigate to url
        getDriver().get(wrappedUrl);
        // get current url
        String result = getDriver().getCurrentUrl();
        // close tab
        getDriver().close();
        // switch to previous tab
        getDriver().switchTo().window(tabs.get(0));
        return result;
    }

    private static boolean isWrapped(String url) {
        return url.contains("yandex.ru/count/");
    }
}
