package scripts.impl;

import checkUnits.CheckUnit;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import scripts.RobotScript;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class SearchScript extends RobotScript {

    /** Максимальное кол-во проверяемых результатов по умолчанию */
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    /** Максимальное кол-во проверяемых результатов */
    private int searchResultLimit;

    /** Текущее кол-во проверенных результатов */
    private int checkedResultCount;

    private EqualityTest test;

    @BeforeClass
    @Parameters({"searchResultLimit"})
    public void setOptions(String searchLimit) throws URISyntaxException {
        this.test = EqualityTest.forCheckUnit(getCheckUnit());
        this.checkedResultCount = 0;

        int limit = Integer.parseInt(searchLimit);
        this.searchResultLimit = limit > 0 ? limit : DEFAULT_SEARCH_LIMIT;
    }

    EqualityTest getEqualityTest() {
        return test;
    }

    ExecutionPSJobResult checkSearchResult() throws RobotScriptExecutionException {
        do {
            if (captcha())
                throw new Captcha_RobotScriptExecutionException("Обнаружена captcha");

            List<WebElement> elements = collectLinkElements();

            for (WebElement element : elements) {
                String href = element.getAttribute("href");
                log.info(checkedResultCount + ". " + href);

                if (test.equalTo(href)) {
                    scrollTo(element);
                    return createExecutionResult(true);
                }

                ++checkedResultCount;
            }

        } while (checkedResultCount < searchResultLimit && nextPage());

        return createExecutionResult(false);
    }

    ExecutionPSJobResult createExecutionResult(boolean linkFound) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setJobID(Long.parseLong(getJobID()));
        message.setCheckUnit(getCheckUnit());
        message.setLinkFound(linkFound);
        message.setScreenshot(linkFound ? takeScreenshot() : null);
        return message;
    }

    boolean nextPage(By by) {
        WebElement next = findElementIfExists(by);
        if (next != null) {
            next.click();
            new WebDriverWait(driver, 20)
                    .until(waitDriver -> ((JavascriptExecutor) waitDriver)
                            .executeScript("return document.readyState")
                            .equals("complete"));
            return true;
        }
        return false;

    }

    @Nullable
    WebElement findElementIfExists(By by, WebElement where) {
        List<WebElement> list = where.findElements(by);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    @Nullable
    private WebElement findElementIfExists(By by) {
        List<WebElement> list = driver.findElements(by);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    private void scrollTo(WebElement webElement) {
        Actions actions = new Actions(driver);
        actions.moveToElement(webElement, 0, 0).perform();
    }

    private byte[] takeScreenshot() {
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
        return takesScreenshot.getScreenshotAs(OutputType.BYTES);
    }



    protected abstract boolean nextPage();

    protected abstract boolean captcha();

    protected abstract List<WebElement> collectLinkElements();



    protected interface EqualityTest {

        boolean equalTo(String found);

        @Nullable
        static URI toUri(String url) {
            try {
                if (Objects.nonNull(url)) {
                    url = URLDecoder.decode(url,
                            StandardCharsets.UTF_8.name());
                    return new URI(url);
                }
            } catch (URISyntaxException e) {
                log.warn(e.getMessage() + ": " + url);
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
            return null;
        }

        static EqualityTest forCheckUnit(CheckUnit unit) throws URISyntaxException {
            switch (unit.getType()) {

                case DOMAIN_MASK:
                case IP_V4_SUBNET:
                case IP_V6_SUBNET:
                    throw new IllegalArgumentException(
                            "Link comparison not implemented for type=" +
                                    unit.getType().toString());

                case URL:
                    return new UriEquality(unit.getValue());

                default:
                    return new HostEquality(unit.getValue());
            }
        }
    }

    private static class HostEquality implements EqualityTest {

        private String forbiddenHost;

        public HostEquality(String forbiddenHost) {
            this.forbiddenHost = forbiddenHost;
        }

        @Override
        public boolean equalTo(String found) {
            URI foundUri = EqualityTest.toUri(found);
            return foundUri != null &&
                    forbiddenHost.equals(foundUri.getHost());
        }
    }

    private static class UriEquality implements EqualityTest {

        private URI forbiddenUri;

        public UriEquality(String forbidden) throws URISyntaxException {
            this.forbiddenUri = new URI(forbidden);
        }

        @Override
        public boolean equalTo(String found) {
            URI foundUri = EqualityTest.toUri(found);
            return forbiddenUri.equals(foundUri);
        }
    }

}
