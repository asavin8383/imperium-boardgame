package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.RobotDriverParameters;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "robots")
public class CommonDirectSearchRobot extends SeleniumRobot {

    public enum ResultPageType {
        PAGINATION,
        CONTINUOUS
    }

    /** Время ожидания загрузки результатов (сек) */
    private static final int SEARCH_RESULT_TIMEOUT = 3;

    /** Максимальное кол-во проверяемых результатов по умолчанию */
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    /** Задержка при вводе по умолчанию (мс) */
    private static final long DEFAULT_INPUT_DELAY = 0;



    /** Максимальное кол-во проверяемых результатов */
    private int searchResultLimit;

    /** Задержка при вводе (мс) */
    private long inputDelay;

    /** URL поисковой системы */
    private String searchSystemUrl;

    /** xpath поисковой строки */
    private String xpathInputField;

    /** xpath элемента, по которому
     * опредеяется наличие капчи */
    private String xpathCaptcha;

    /** xpath элемента, по клику на который
     * происходит переход на следующую страницу */
    private String xpathNextPage;

    /** xpath тегов \<a>, содержащих
     *  проверяемые ссылки (href) */
    private String xpathItemLink;

    /** Тип страницы результатов
     * (с переходом по страницам или
     * кнопкой "показать больше" */
    private ResultPageType resultPageType;



    /** Сверяет ссылку с проверяемой */
    protected EqualityTest equalityTest;

    /** Кол-во проверенных ссылок */
    protected Integer counter = 0;



    public CommonDirectSearchRobot(RobotDriverParameters driverParams,
                            Map<AccessToolParameters, String> scriptParams,
                            int searchLimit) {

        super(driverParams, scriptParams);
        setParams(scriptParams, searchLimit);
    }

    private void setParams(Map<AccessToolParameters, String> scriptParams, int searchLimit) {
        this.searchResultLimit = searchLimit > 0 ? searchLimit : DEFAULT_SEARCH_LIMIT;

        long delayParameterValue = scriptParams.containsKey(AccessToolParameters.INPUT_DELAY) ?
                Long.parseLong(scriptParams.get(AccessToolParameters.INPUT_DELAY)) : DEFAULT_INPUT_DELAY;
        this.inputDelay = delayParameterValue < 0 ? DEFAULT_INPUT_DELAY : delayParameterValue;

        String strType = scriptParams.get(AccessToolParameters.SEARCH_SYSTEM_RESULT_PAGE_TYPE);
        this.resultPageType = strType != null ?
                ResultPageType.valueOf(strType.toUpperCase()) : ResultPageType.PAGINATION;

        this.searchSystemUrl = scriptParams.get(AccessToolParameters.SEARCH_SYSTEM_URL);
        this.xpathInputField = scriptParams.get(AccessToolParameters.SEARCH_SYSTEM_XPATH_INPUT_FIELD);
        this.xpathCaptcha = scriptParams.get(AccessToolParameters.SEARCH_SYSTEM_XPATH_CAPTCHA);
        this.xpathNextPage = scriptParams.get(AccessToolParameters.SEARCH_SYSTEM_XPATH_NEXT_PAGE);
        this.xpathItemLink = scriptParams.get(AccessToolParameters.SEARCH_SYSTEM_XPATH_ITEM_LINK);
    }



    public int getSearchResultLimit() {
        return searchResultLimit;
    }

    public long getInputDelay() {
        return inputDelay;
    }

    public String getSearchSystemUrl() {
        return searchSystemUrl;
    }

    public String getXpathInputField() {
        return xpathInputField;
    }

    public String getXpathCaptcha() {
        return xpathCaptcha;
    }

    public String getXpathNextPage() {
        return xpathNextPage;
    }

    public String getXpathItemLink() {
        return xpathItemLink;
    }

    public ResultPageType getResultPageType() {
        return resultPageType;
    }



    final ExecutionPSJobResult createMessage(boolean linkFound) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setLinkFound(linkFound);
        message.setError(false);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }



    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException {
        driver.get(searchSystemUrl);
        searchFor(checkUnit.getValue());

        if (captcha())
            throw new Captcha_ExecutionException(
                    "Обнаружена Captcha " + driver.getCurrentUrl());

        equalityTest = EqualityTest.forCheckUnit(checkUnit);

        boolean isViolation = resultPageType == ResultPageType.PAGINATION?
                checkPaginatedSearchResult() : checkContinuousSearchResult();
        return createMessage(isViolation);
    }

    boolean captcha() {
        return xpathCaptcha != null && xpathCaptcha.length() > 0 &&
                driver.findElements(By.xpath(xpathCaptcha)).size() > 0;
    }

    boolean checkContinuousSearchResult() throws ExecutionException {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        By linkLocator = By.xpath(xpathItemLink);

        int initResultCount = driver.findElements(linkLocator).size();
        int buttonClickCount = (searchResultLimit - 1) / initResultCount;

        ScriptUtils.scrollToBottom(jsExecutor);
        for (int i = 0; i < buttonClickCount && nextPage(); i++) {
            ScriptUtils.scrollToBottom(jsExecutor);
            ScriptUtils.waitPageLoading(driver);
        }

        List<WebElement> links = driver.findElements(linkLocator);
        while(links.size() < searchResultLimit && nextPage()) {
            ScriptUtils.scrollToBottom(jsExecutor);
            ScriptUtils.waitPageLoading(driver);
            links = driver.findElements(linkLocator);
        }

        return checkPageResult(links);
    }

    boolean checkPaginatedSearchResult() throws ExecutionException {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        do {
            ScriptUtils.waitPageLoading(driver);
            if (checkPageResult())
                return true;

            // for yahoo before clicking next page element
            ScriptUtils.scrollToBottom(jsExecutor);

        } while (counter < searchResultLimit && nextPage());

        return false;
    }

    String extractUrl(WebElement element) {
        return element.getAttribute("href");
    }



    private void searchFor(String value) {
        WebElement inputField = driver.findElement(
                By.xpath(xpathInputField));
        ScriptUtils.type(inputField, inputDelay, value);
        inputField.sendKeys(Keys.ENTER);
    }

    private boolean checkPageResult() {
        try {
            return checkPageResult(new WebDriverWait(driver, SEARCH_RESULT_TIMEOUT)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xpathItemLink))));
        } catch (TimeoutException e) {
            log.debug("Results not found");
            return false;
        }
    }

    private boolean checkPageResult(List<WebElement> links) {
        Iterator<WebElement> it = links.iterator();
        while (it.hasNext() && counter++ < searchResultLimit) {
            WebElement link = it.next();
            String url = extractUrl(link);
            // System.out.println(counter + ". " + url);
            try {
                if (equalityTest.equalTo(url)) {
                    scrollTo(link);
                    return true;
                }
            } catch (MalformedURLException e) {
                log.warn("Ошибка при проверке ссылки '{}' в ({}): {}",
                        url, searchSystemUrl, e.getLocalizedMessage());
            }
        }
        return false;
    }

    private void scrollTo(WebElement webElement) {
        Actions actions = new Actions(driver);
        actions.moveToElement(webElement, 0, 0).perform();
    }

    private boolean nextPage() throws TimeoutScriptException {
        WebElement next = ScriptUtils.findElementIfExists(
                By.xpath(xpathNextPage), driver);
        try {
            if (next != null) {
                next.click();
                return true;
            }
        } catch (TimeoutException e) {
            throw new TimeoutScriptException(e);
        }
        return false;
    }

}
