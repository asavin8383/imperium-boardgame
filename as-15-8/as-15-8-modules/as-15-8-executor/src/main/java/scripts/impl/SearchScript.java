package scripts.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.util.StringUtils;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionPSJobResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

@Slf4j
public abstract class SearchScript extends RobotScript {

	/** Максимальное кол-во проверяемых результатов по умолчанию */
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    private static final long DEFAULT_INPUT_DELAY = 0;

    /** Максимальное кол-во проверяемых результатов */
    private int searchResultLimit;

    /** Текущее кол-во проверенных результатов */
    private int checkedResultCount;

    private long inputDelay;
    
	public SearchScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, int searchLimit, long inputDelay)
			throws MalformedURLException {
		
		super(driverParams, scriptParams);
		this.checkedResultCount = 0;

		this.searchResultLimit = searchLimit > 0 ? searchLimit : DEFAULT_SEARCH_LIMIT;

		this.inputDelay = inputDelay > 0 ? inputDelay : DEFAULT_INPUT_DELAY;
	}

    protected long getInputDelay() {
        return inputDelay;
    }

    ExecutionPSJobResult checkSearchResult(EqualityTest test) throws RobotScriptExecutionException {
        do {
            if (captcha())
                throw new Captcha_RobotScriptExecutionException("Обнаружена captcha");

            List<WebElement> elements = collectLinkElements();

            for (WebElement element : elements) {
                String href = element.getAttribute("href");
                //log.info(checkedResultCount + ". " + href);

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
        message.setLinkFound(linkFound);
        message.setScreenshot(linkFound ?
                ScriptUtils.getScreenshot(driver) : null);
        return message;
    }

    boolean nextPage(By by) {
        WebElement next = findElementIfExists(by);
        if (next != null) {
            next.click();
            ScriptUtils.waitPageLoading(driver);
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



    protected abstract boolean nextPage();

    protected abstract boolean captcha();

    protected abstract List<WebElement> collectLinkElements();



    protected interface EqualityTest {

        boolean equalTo(String found);

        @SneakyThrows
        static String decode(String url) {
            if (!StringUtils.isEmpty(url)) {
                return URLDecoder.decode(url,
                        StandardCharsets.UTF_8.name());
            }
            return url;
        }

        @Nullable
        static URL toUrl(String url) {
            try {
                if (!StringUtils.isEmpty(url)) {
                    return new URL(decode(url));
                }
            } catch (MalformedURLException e) {
                log.warn(e.getMessage() + ": " + url);
            }
            return null;
        }

        static EqualityTest forCheckUnit(CheckUnit unit) {
            switch (unit.getType()) {

                case DOMAIN_MASK:
                case IP_V4_SUBNET:
                case IP_V6_SUBNET:
                    throw new IllegalArgumentException(
                            "Link comparison not implemented for type=" +
                                    unit.getType().toString());

                case URL:
                    return new UrlEquality(unit.getValue());
                case IP_V6:
                    return new IPv6HostEquality(unit.getValue());

                default:
                    return new HostEquality(unit.getValue());
            }
        }
    }

    private static class HostEquality implements EqualityTest {

        private String forbiddenHost;

        HostEquality(String forbiddenHost) {
            this.forbiddenHost = forbiddenHost;
        }

        @Override
        public boolean equalTo(String found) {
            URL foundUrl = EqualityTest.toUrl(found);
            return foundUrl != null &&
                    forbiddenHost.equals(foundUrl.getHost());
        }
    }

    private static class UrlEquality implements EqualityTest {

        private String originalUrl;
        private String decodedUrl;
        private boolean isEncoded;

        UrlEquality(String forbiddenUrl) {
            this.originalUrl = removeSlash(forbiddenUrl);
            this.decodedUrl = EqualityTest.decode(originalUrl);
            this.isEncoded = originalUrl.compareTo(decodedUrl) != 0;
        }

        @Override
        public boolean equalTo(String foundUrl) {
            if (StringUtils.isEmpty(foundUrl))
                return false;

            foundUrl = removeSlash(foundUrl);

            if (isEncoded)
                return originalUrl.equals(foundUrl);

            foundUrl = EqualityTest.decode(foundUrl);
            return decodedUrl.equals(foundUrl);
        }

        private String removeSlash(String url) {
            if (url != null && url.length() > 0 &&
                    url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }
    }

    private static class IPv6HostEquality implements EqualityTest {

        private String forbiddenHost;

        IPv6HostEquality(String forbiddenHost) {
            this.forbiddenHost = removeBrackets(forbiddenHost);
        }

        @Override
        public boolean equalTo(String found) {
            URL foundUrl = EqualityTest.toUrl(found);
            if (foundUrl != null) {
                String foundHost = removeBrackets(foundUrl.getHost());
                return forbiddenHost.equals(foundHost);
            }
            return false;
        }

        private String removeBrackets(String host) {
            return host.replaceAll("[\\[\\]]", "");
        }
    }

}
