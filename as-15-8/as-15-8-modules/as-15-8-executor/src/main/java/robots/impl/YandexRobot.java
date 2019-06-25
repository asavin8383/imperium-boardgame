package robots.impl;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;
import robots.utils.EqualityTest;

public class YandexRobot extends SearchRobot {

	private static final String YANDEX_URL = "https://yandex.ru";

	
	public YandexRobot(RobotDriverParameters driverParams,
                        Map<AccessToolParameters, String> scriptParams,
                        int searchLimit) {
		super(driverParams, scriptParams, searchLimit);
	}


	@Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
		EqualityTest test = EqualityTest.forCheckUnit(checkUnit);
		
		driver.get(YANDEX_URL);

        WebElement input = driver.findElement(By.name("text"));
       input(input, checkUnit.getValue() + " ");

        if (checkSuggestedLink(test)) {
            return createExecutionResult(true);
        } else {
            input.sendKeys(Keys.ENTER);
            return checkSearchResult(test);
        }
	}

    private boolean checkSuggestedLink(EqualityTest test) throws RobotScriptExecutionException {
    	try {
	        WebElement element = getSuggestedLink(driver);
	        if (Objects.nonNull(element)) {
	            String href = element.getAttribute("href");
	            return test.equalTo(href);
	        }
	        return false;
    	} catch (Exception ex) {
    		throw new RobotScriptExecutionException(ex);
    	}
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
    protected By nextPageBy() {
        return By.xpath("//div[contains(@class, \"pager \")]/a[last()]");
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
