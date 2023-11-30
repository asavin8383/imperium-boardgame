package robots.impl;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import enums.AccessToolParameter;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.exceptions.BadIP_ExecutionExeption;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j(topic = "robots")
public class CommonDirectSearchRobot extends SeleniumRobot {

    public enum ResultPageType {
        PAGINATION,
        CONTINUOUS
    }

    @Getter
    @Setter
    private int remainingAttempts;

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

    /** Регулярное выражение, определяющее пустой результат
     *  выполнения поискового запроса*/
    private String resultNotFoundRegexp;

    /** Необходимость проверки подсказки ПС */
    private boolean needCheckHint;

    /** CSS класс всплывающей подсказки ПС */
    private String hintCSSClassName;

    /** CSS класс ссылки на сайт во всплывающей подсказке ПС */
    private String hintLinkCSSClassName;

    /** префикс в поисковом запросе ПС */
    private String searchQueryPrefixForUrl;

    /** префикс в поисковом запросе ПС */
    private String searchQueryPrefixForDomain;

    /**
     * Ссылка на возвращение к исходному тексту после правки правописания
     */
    private String checkSpellingLink;

    /** Тип страницы результатов
     * (с переходом по страницам или
     * кнопкой "показать больше" */
    private ResultPageType resultPageType;

    /**
     * Делать ли скриншот, если нарушений не найдено
     */
    private boolean makeScreenShotOnCompleted;



    /** Сверяет ссылку с проверяемой */
    protected EqualityTest equalityTest;

    /** Кол-во проверенных ссылок */
    protected Integer counter = 0;

    public CommonDirectSearchRobot(Map<AccessToolParameter, String> scriptParams) {
        super(scriptParams,
                Strings.isNotEmpty(scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_PROXY)) ?
                        scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_PROXY) : null);
        setParams(scriptParams);
    }

    private void setParams(Map<AccessToolParameter, String> scriptParams) {
        this.searchResultLimit = scriptParams.containsKey(AccessToolParameter.SEARCH_RESULT_LIMIT) ?
                Integer.parseInt(scriptParams.get(AccessToolParameter.SEARCH_RESULT_LIMIT)) : DEFAULT_SEARCH_LIMIT;

        long delayParameterValue = scriptParams.containsKey(AccessToolParameter.INPUT_DELAY) ?
                Long.parseLong(scriptParams.get(AccessToolParameter.INPUT_DELAY)) : DEFAULT_INPUT_DELAY;
        this.inputDelay = delayParameterValue < 0 ? DEFAULT_INPUT_DELAY : delayParameterValue;

        String strType = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_RESULT_PAGE_TYPE);
        this.resultPageType = strType != null ?
                ResultPageType.valueOf(strType.toUpperCase()) : ResultPageType.PAGINATION;

        this.searchSystemUrl = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_URL);
        this.xpathInputField = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_XPATH_INPUT_FIELD);
        this.xpathCaptcha = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_XPATH_CAPTCHA);
        this.xpathNextPage = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_XPATH_NEXT_PAGE);
        this.xpathItemLink = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_XPATH_ITEM_LINK);
        this.resultNotFoundRegexp = scriptParams.get(AccessToolParameter.RESULT_NOT_FOUND_REGEXP);
        this.checkSpellingLink = scriptParams.get(AccessToolParameter.CHECK_SPELLING_LINK);

        String needCheckHintStr = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_CHECK_HINT);
        this.needCheckHint = Strings.isNotEmpty(needCheckHintStr) && Boolean.parseBoolean(needCheckHintStr);
        this.hintCSSClassName = scriptParams.get(AccessToolParameter.HINT_CLASS_NAME);
        this.hintLinkCSSClassName = scriptParams.get(AccessToolParameter.HINT_LINK_CLASS_NAME);

        this.searchQueryPrefixForUrl = scriptParams.get(AccessToolParameter.SEARCH_PREFIX_FOR_URL);
        this.searchQueryPrefixForDomain = scriptParams.get(AccessToolParameter.SEARCH_PREFIX_FOR_DOMAIN);

        this.makeScreenShotOnCompleted = !scriptParams.containsKey(AccessToolParameter.MAKE_SCREENSHOT_ON_COMPLETED)
            || Boolean.parseBoolean(scriptParams.get(AccessToolParameter.MAKE_SCREENSHOT_ON_COMPLETED));
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


    final ExecutionPSJobResult createMessage(boolean linkFound, CheckUnitJobResult checkUnitJobResult) {
        return createMessage(linkFound, checkUnitJobResult, null);
    }
    final ExecutionPSJobResult createMessage(boolean linkFound, CheckUnitJobResult checkUnitJobResult, List<String> urls) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setLinkFound(linkFound);
        message.setError(false);
        if(linkFound || this.makeScreenShotOnCompleted){
            message.setScreenshot(ScriptUtils.getScreenshot(driver));
        }
        message.setUrls(urls);
        message.setCheckUnitJobResult(checkUnitJobResult);
        return message;
    }


    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException {
        driver.get(searchSystemUrl);
        equalityTest = EqualityTest.forCheckUnit(checkUnit);

        if (captcha())
            if (throwExceptionByCaptchaOrBadIP) {
                throw new Captcha_ExecutionException(String.format("ПС выдала капчу на url: %s", checkUnit.getValue()));
            } else {
                return createMessage(false, CheckUnitJobResult.CAPTCHA_DETECTED);
            }

        try {
            driver.findElement(By.xpath(xpathInputField));
        } catch(NoSuchElementException ex) {
            if (throwExceptionByCaptchaOrBadIP) {
                throw new BadIP_ExecutionExeption(String.format("ПС выдала BAD IP на url: %s", checkUnit.getValue()));
            } else {
                return createMessage(false, CheckUnitJobResult.BAD_IP);
            }
        }

        String value = checkUnit.getValue();

        if(checkUnit.getType().equals(CheckUnitType.URL) && Strings.isNotEmpty(searchQueryPrefixForUrl))
            value = searchQueryPrefixForUrl + value;
        if(checkUnit.getType().equals(CheckUnitType.DOMAIN) && Strings.isNotEmpty(searchQueryPrefixForDomain))
            value = searchQueryPrefixForDomain + value;

        if(this.needCheckHint) {
            if (checkHintAndSearch(value))
                return createMessage(true, CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED);
        } else {
            searchText(value);
        }

        if (captcha())
            if (throwExceptionByCaptchaOrBadIP) {
                throw new Captcha_ExecutionException(String.format("ПС выдала капчу на url: %s", checkUnit.getValue()));
            } else {
                return createMessage(false, CheckUnitJobResult.CAPTCHA_DETECTED);
            }

        //Проверим, не исправилось ли правописание. Если исправилось, возвращаем назад
        if(Strings.isNotEmpty(this.checkSpellingLink)){
            WebElement spellingLink = ScriptUtils.findElementIfExists(
                By.xpath(this.checkSpellingLink), driver);
            try {
                if (spellingLink != null) {
                    spellingLink.click();
                }
            } catch (TimeoutException e) {
                throw new TimeoutScriptException(e);
            }
        }

        //Проверяем, не вылез ли подозрительный трафик
        Optional<ExecutionJobResult> optExecutionJobResult = checkNoLinks(checkUnit, throwExceptionByCaptchaOrBadIP);
        if(optExecutionJobResult.isPresent()){
            return optExecutionJobResult.get();
        }

        if (checkUnit.getType() == CheckUnitType.SEARCH_PHRASE){
            List<String> hintLinks = new ArrayList<>();
            for (WebElement w : getLinks(resultPageType)) {
                try {
                    hintLinks.add(extractUrl(w));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            return createMessage(false, null, hintLinks);
        }


        boolean isViolation;
        if (resultPageType == ResultPageType.PAGINATION) {
            isViolation = checkPaginatedSearchResult();
        } else if (resultPageType == ResultPageType.CONTINUOUS){
            isViolation = checkContinuousSearchResult();
        } else {
            throw new ExecutionException("Ошибка проверки ПС! Недопустимый тип страницы: " + resultPageType);
        }
        return createMessage(isViolation, null);
    }

    private boolean linksFound(){
        ScriptUtils.waitPageLoading(driver);
        By linkLocator = By.xpath(xpathItemLink);
        List<WebElement> listNext = driver.findElements(linkLocator);
        return (listNext != null && listNext.size() > 0);
    }

    private Optional<ExecutionJobResult> checkNoLinks(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP){
        //Проверяем, не вылез ли подозрительный трафик
        if(!linksFound()) {
            log.debug("По URL {} Список ссылок пустой нужно проверить на пустой IP", checkUnit.getValue());
            if(Strings.isNotEmpty(resultNotFoundRegexp)) {
                String content = ScriptUtils.getPageSource(driver).pageSource;
                if(content==null) {
                    // Завис скрипт, ставим сомнительный результат
                    return Optional.of(createMessage(false, CheckUnitJobResult.DOUBTFUL));
                }
                //log.info("Контент страницы: {}", content);
                log.debug("Регулярное выражение для проверки: {}", resultNotFoundRegexp);
                Matcher matcher = Pattern.compile(resultNotFoundRegexp).matcher(content);
                if(matcher.find()){
                    // Ссылок не найдено
                    return Optional.of(createMessage(false, CheckUnitJobResult.COMPLETED));
                } else {
                    //Плохой IP
                    if (throwExceptionByCaptchaOrBadIP) {
                        throw new BadIP_ExecutionExeption(String.format("ПС выдала BAD IP на url: %s", checkUnit.getValue()));
                    } else {
                        return Optional.of(createMessage(false, CheckUnitJobResult.BAD_IP));
                    }
                }
            } else {
                //регулярку не задали, считаем плохим IP
                if (throwExceptionByCaptchaOrBadIP) {
                    throw new BadIP_ExecutionExeption(String.format("ПС выдала BAD IP на url: %s", checkUnit.getValue()));
                } else {
                    return Optional.of(createMessage(false, CheckUnitJobResult.BAD_IP));
                }
            }
        }
        return Optional.empty();
    }


    private List<WebElement> getLinks(ResultPageType resultPageType){
        return resultPageType == ResultPageType.PAGINATION?
            getPaginatedLinks() : getContinuousLinks();
    }

    boolean captcha() {
        return xpathCaptcha != null && xpathCaptcha.length() > 0 &&
                driver.findElements(By.xpath(xpathCaptcha)).size() > 0;
    }

    List<WebElement> getContinuousLinks() throws ExecutionException {
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

        return links;
    }

    List<WebElement> getPaginatedLinks() throws ExecutionException {
        List<WebElement> links = new ArrayList<>();
        do {
            ScriptUtils.waitPageLoading(driver);
            By linkLocator = By.xpath(xpathItemLink);
            List<WebElement> listNext = driver.findElements(linkLocator);

            if(listNext != null)
                links.addAll(listNext);

        }
        while (links.size() < searchResultLimit && nextPage());
        return links;
    }

    private boolean checkContinuousSearchResult() throws ExecutionException {
        List<WebElement> links = getContinuousLinks();
        return checkPageResult(links);
    }

    boolean checkPaginatedSearchResult() throws ExecutionException {
        do {
            ScriptUtils.waitPageLoading(driver);
            if (checkPageResult())
                return true;
        } while (counter < searchResultLimit && nextPage());

        return false;
    }

    String extractUrl(WebElement element) {
        return element.getAttribute("href");
    }

    private void searchText(String value) {
        WebElement inputField = driver.findElement(
                By.xpath(xpathInputField));
        ScriptUtils.type(inputField, inputDelay, value);
        inputField.sendKeys(Keys.ENTER);
    }

    private boolean checkHintAndSearch(String value) {
        try {
            WebElement inputField = driver.findElement(
                    By.xpath(xpathInputField));
            observeHintLinks();
            if(typeAndCheckHint(inputField, value, inputDelay))
                return true;
            inputField.sendKeys(Keys.ENTER);
            return false;
        } catch (Exception ex){
            throw new ExecutionException("Ошибка при поиске в поисковой системе", ex);
        }
    }

    private boolean typeAndCheckHint(WebElement inputField, String query, long sleep) {

        for (char cp : query.toCharArray()) {
            inputField.sendKeys(new String(
                    Character.toChars(cp)));
            if(checkHintLinks())
                return true;
            if(sleep > 0L) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {}
            }
        }
        return false;
    }

    private boolean checkHintLinks() {
        List<String> links = getHintLinks();
        for(String link : links) {
            try {
                if(equalityTest.equalTo(link)){
                    stopHintLinksObserver();
                    return true;
                }
            } catch (Exception ex) {
                log.error("Ошибка сверки результатов", ex);
            }
        }
        return false;
    }

    private void observeHintLinks() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = ScriptUtils.getScriptFromResource("observeHintLinks.js");
        js.executeScript(script, this.hintCSSClassName, this.hintLinkCSSClassName);
    }

    private List<String> getHintLinks() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = ScriptUtils.getScriptFromResource("getHintLinks.js");
        return Arrays.asList(js.executeScript(script).toString().split(", "));
    }

    private void stopHintLinksObserver() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = ScriptUtils.getScriptFromResource("stopHintLinksObserver.js");
        js.executeScript(script);
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
            log.debug("Проверяется ссылка: {}", url);
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
        //Переходим в низ страницы
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        ScriptUtils.scrollToBottom(jsExecutor);
        //Жмём на кнопку next
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
