package scripts.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class YandexScript extends SearchScript {

    private static final String YANDEX_URL = "https://yandex.ru";

    @Override
    public void execute() throws RobotScriptExecutionException {
        driver.get(YANDEX_URL);
        driver.manage().window().fullscreen();

        WebElement input = driver.findElement(By.name("text"));
        ScriptUtils.type(input, getInputDelay(),
                getCheckUnit().getValue() + " ");
        //input.sendKeys(getCheckUnit().getValue() + " ");

        if (checkSuggestedLink(driver)) {
            sendExecutionResult(createExecutionResult(true));
        } else {
            input.sendKeys(Keys.ENTER);
            sendExecutionResult(checkSearchResult());
        }
    }

    private boolean checkSuggestedLink(WebDriver driver) {
        WebElement element = getSuggestedLink(driver);
        if (Objects.nonNull(element)) {
            String href = element.getAttribute("href");
            return getEqualityTest().equalTo(href);
        }
        return false;
    }

    @Nullable
    private WebElement getSuggestedLink(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 1);
            return wait.until(presenceOfElementLocated(
                    By.className("suggest2-item__link")));
        } catch (Exception e) {
            // ignore
            return null;
        }
    }

    @Override
    protected boolean nextPage() {
        return nextPage(By.xpath("//div[contains(@class, \"pager \")]/a[last()]"));
    }

    @Override
    protected boolean captcha() {
        // todo yandex captcha
        return false;
    }

    @Override
    protected List<WebElement> collectLinkElements() {
        String xpathListLinks = "//li[@class=\"serp-item\"]//div[contains(@class, \"organic__path\")]";
        String xpathMainLinks = "//div[@class=\"serp-item\"]//div[contains(@class, \"organic__path\")]/a";

        List<WebElement> links = driver.findElements(By.xpath(xpathMainLinks));
        driver.findElements(By.xpath(xpathListLinks))
                .forEach(element -> {
                    List<WebElement> listLinks = element.findElements(By.xpath("./a[2]"));
                    if (listLinks.isEmpty()) {
                        listLinks = element.findElements(By.xpath("./a[1]"));
                    }
                    links.addAll(listLinks);
                });

        return links;
    }

}
