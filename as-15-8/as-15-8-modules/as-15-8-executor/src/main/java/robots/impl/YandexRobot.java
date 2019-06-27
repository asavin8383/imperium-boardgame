package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static robots.utils.ScriptUtils.findElementIfExists;

@Slf4j(topic = "robots")
public class YandexRobot extends SearchRobot {

    private static final int LOADING_PAGE_TIMEOUT = 60;

	private static final String YANDEX_URL = "https://yandex.ru";

    private static final String XPATH_LIST_ELEM = "//li[@class=\"serp-item\"]//div[contains(@class, \"organic__path\")]";
    private static final String XPATH_MAIN_LINK = "//div[@class=\"serp-item\"]//div[contains(@class, \"organic__path\")]/a";
	
	public YandexRobot(RobotDriverParameters driverParams,
                        Map<AccessToolParameters, String> scriptParams,
                        int searchLimit) {
		super(driverParams, scriptParams, searchLimit);
	}

	@Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        driver.manage().timeouts().pageLoadTimeout(LOADING_PAGE_TIMEOUT, TimeUnit.SECONDS);

		EqualityTest test = EqualityTest.forCheckUnit(checkUnit);
		
		driver.get(YANDEX_URL);

        WebElement inputField = driver.findElement(By.name("text"));
        input(inputField, checkUnit.getValue() + " ");

        WebElement suggestedLink = getSuggestedLink(driver);
        if (suggestedLink != null) {
            byte[] screenshot = ScriptUtils.getScreenshot(driver);

            if (checkLink(suggestedLink, test)) {
                ExecutionPSJobResult message =
                        new ExecutionPSJobResult();
                message.setLinkFound(true);
                message.setScreenshot(screenshot);
                return message;
            }
        }

        inputField.sendKeys(Keys.ENTER);
        return createMessage(checkSearchResult(test));
	}

    @Override
    protected By nextPageLocator() {
        return By.xpath("//div[contains(@class, \"pager \")]/a[last()]");
    }

    @Override
    protected boolean captcha() {
	    WebElement captchaForm1 = findElementIfExists(
	            By.className("form__captcha-wrapper"), driver);

	    WebElement captchaForm2 = findElementIfExists(
	            By.className("form__captcha"), driver); // "image form__captcha"

        return captchaForm1 != null || captchaForm2 != null;
    }

    @Override
    protected boolean checkPageResult(EqualityTest test) {
        if (checkLink(findElementIfExists(By.xpath(XPATH_MAIN_LINK), driver), test))
            return true;

        List<WebElement> divs = driver.findElements(By.xpath(XPATH_LIST_ELEM));
        Iterator<WebElement> it = divs.iterator();
        while (it.hasNext() && withinLimit()) {
            WebElement div = it.next();
            WebElement link = extractLink(div);

            if (link == null) {
                log.warn("Не удалось извлечь ссылку из div (Yandex): {}",
                        div.getAttribute("outerHTML"));
            } else if (checkLink(link, test)) {
                scrollTo(link);
                return true;
            }

            incCheckResultCount();
        }
        return false;
    }

    private boolean checkLink(@Nullable WebElement element, EqualityTest test) {
        if (element == null)
            return false;

        String url = element.getAttribute("href");
        try {
            return test.equalTo(isWrapped(url) ?
                    getDirectUrl(url) : url);

        } catch (TimeoutException | MalformedURLException e) {
            log.warn("Ошибка при проверке ссылки '{}' Yandex: {}",
                    url, e.getLocalizedMessage());
        }
        return false;
    }

    @Nullable
    private WebElement getSuggestedLink(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver,
                    1);
            return wait.until(presenceOfElementLocated(
                    By.className("suggest2-item__link")));
        } catch (TimeoutException | NoSuchElementException e) {
            // ignore
            return null;
        }
    }

    @Nullable
    private WebElement extractLink(WebElement div) {
        WebElement a = findElementIfExists(By.xpath("./a[2]"), div);
        return a != null ? a : findElementIfExists(By.xpath("./a[1]"), div);
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
        return url.contains("yandex.ru/clck/");
	}
}
