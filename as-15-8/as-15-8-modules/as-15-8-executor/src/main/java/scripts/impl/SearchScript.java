package scripts.impl;

import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import scripts.RobotScript;

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

    /** Искомая ссылка, преобразованная в URI */
    private URI forbidden;

    @BeforeClass
    @Parameters({"searchResultLimit"})
    public void setOptions(String searchLimit) throws URISyntaxException {
        this.checkedResultCount = 0;

        int limit = Integer.parseInt(searchLimit);
        this.searchResultLimit = limit > 0 ?
                limit : DEFAULT_SEARCH_LIMIT;

        // ссылки с '/" и без считаются разными ссылками ?
        String url = removeTrailingSlash(getCheckUnit().getValue());
        this.forbidden = new URI(url);
    }

    /*@Override
    protected void sendExecutionResult(ExecutionJobResult jobResult) throws RobotScriptExecutionException {
        log.info("limit: {}", searchResultLimit);
        ExecutionPSJobResult result = (ExecutionPSJobResult) jobResult;
        Assert.assertFalse(result.isLinkFound());
    }*/

    ExecutionPSJobResult checkSearchResult() {
        do {
            if (captcha())
                return createCaptchaDetectedResult();

            List<WebElement> elements = collectLinkElements();

            for (WebElement element : elements) {
                String href = element.getAttribute("href");
//                log.info(checkedResultCount + ". " + href);

                URI uri = toUri(href);
                if (Objects.nonNull(uri) && isLinkFound(uri)) {
                    scrollTo(element);
                    return createLinkFoundResult();
                }

                ++checkedResultCount;
            }

        } while (checkedResultCount < searchResultLimit && nextPage());

        return createLinkNotFoundResult();
    }

    @Nullable
    private URI toUri(String url) {
        try {
            if (Objects.nonNull(url)) {
                url = URLDecoder.decode(url,
                        StandardCharsets.UTF_8.name());
                return new URI(removeTrailingSlash(url));
            }
        } catch (URISyntaxException e) {
            log.warn(e.getMessage() + ": " + url);
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return null;
    }

    private boolean isLinkFound(URI found) {
        switch (getCheckUnit().getType()) {
            case URL:
                return forbidden.equals(found);
            case DOMAIN:
                return forbidden.getHost().equals(found.getHost());
            default:
                throw new IllegalArgumentException(
                        "Link comparison not implemented for type=" +
                                getCheckUnit().getType().toString());
        }
    }

    private void scrollTo(WebElement webElement) {
        Actions actions = new Actions(driver);
        actions.moveToElement(webElement, 0, 0).perform();
    }

    private String removeTrailingSlash(String url) {
        if (url != null && url.length() > 0 &&
                url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private byte[] takeScreenshot() {
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
        return takesScreenshot.getScreenshotAs(OutputType.BYTES);
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

    ExecutionPSJobResult createCaptchaDetectedResult() {
        return createExecutionResult(false, true, takeScreenshot());
    }

    ExecutionPSJobResult createLinkFoundResult() {
        return createExecutionResult(true, false, takeScreenshot());
    }

    ExecutionPSJobResult createLinkNotFoundResult() {
        return createExecutionResult(false, false, null);
    }

    private ExecutionPSJobResult createExecutionResult(boolean linkFound,
                                                       boolean captchaDetected,
                                                       byte[] screenshot) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setJobID(Long.parseLong(getJobID()));
        message.setCheckUnit(getCheckUnit());
        message.setLinkFound(linkFound);
        message.setCaptchaDetected(captchaDetected);
        message.setScreenshot(screenshot);
        return message;
    }

    @Nullable
    private WebElement findElementIfExists(By by) {
        List<WebElement> list = driver.findElements(by);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    @Nullable
    WebElement findElementIfExists(By by, WebElement where) {
        List<WebElement> list = where.findElements(by);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }



    protected abstract boolean nextPage();

    protected abstract boolean captcha();

    protected abstract List<WebElement> collectLinkElements();
}
