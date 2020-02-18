package robots.utils;

import checkUnits.CheckUnit;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.ChromeSettings;
import robots.exceptions.ExecutionException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class ScriptUtils {

    public static final String TIME_OUT_ERROR = "TIME_OUT";
    public static final String TIME_OUT_CHECKING_ERROR = "TIME_OUT_CHECKING";

    private static final int ERROR_PAGE_SIZE_THRESHOLD = 2048;

    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageResult {
        public String pageSource;
        public String errorCodeChrome;
    }

    private static Integer WAIT_TIMEOUT = 30000;
    private static Integer WAIT_DRIVER_DEFAULT = 3;


    public static void waitDriver(WebDriver driver){
        waitDriver(driver, WAIT_DRIVER_DEFAULT);
    }

    public static void waitDriver(WebDriver driver, Integer seconds){
        try{
            WebDriverWait wait = new WebDriverWait(driver, seconds);
            wait.until(webDriver -> false);
        }
        catch (TimeoutException ignored){}
    }

    public static void waitPageLoading(WebDriver driver) {
        waitPageLoading(driver, WAIT_TIMEOUT);
    }

    public static void waitPageLoading(WebDriver driver, int timeoutInSeconds) {
        new WebDriverWait(driver, timeoutInSeconds)
                .withMessage("загрузка страницы")
                .until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
    }

    private static String getErrorCode(WebDriver driver){
        try{
            WebElement errorElement = driver.findElement(By.cssSelector("#main-message #error-information-popup-container .error-code"));
            return errorElement.getText();
        }
        catch (NoSuchElementException e){
            return null;
        }
    }

    public static PageResult getPageSource(WebDriver driver){
        String errorCode = getErrorCode(driver);
        String pageContent = errorCode == null ? driver.getPageSource() : null;
        return new PageResult(pageContent, errorCode);
    }

    public static byte[] getScreenshot(WebDriver webDriver)  {
        try{
            switchToTab(webDriver, 2);
            WebDriverWait wait = new WebDriverWait(webDriver, WAIT_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("screen"))).click();

            ScriptUtils.waitForTab(webDriver, 2);
            switchToTab(webDriver, 2);

            String screenSrc = wait
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//img[@src and string-length(@src)!=0]")))
                    .getAttribute("src");

            if(Strings.isEmpty(screenSrc))
                throw new ExecutionException("Ошибка получения скриншота: не найдено данных изображения");

            String[] screenSrcSplit = screenSrc.split(",");
            String base64Image = screenSrcSplit[screenSrcSplit.length - 1];

            // close screenshot tab
            webDriver.close();
            switchToTab(webDriver, 1);

            return Base64.getDecoder().decode(base64Image);
        }catch(Exception ex){
            throw new RuntimeException("Ошибка получения скриншота", ex);
        }
    }

    public static String getCurrentUrl(WebDriver webDriver) {
        try{
            String url = webDriver.getCurrentUrl();
            url = url == null ? "" : (url.startsWith("data:") ? "" : url);
            return url;
        }
        catch(TimeoutException e){
            log.info("TimeoutException при получении currentUrl");
            e.printStackTrace();
            return null;
        }
    }

    public static String getCheckUnitValue(@NonNull CheckUnit checkUnit){
        String value = checkUnit.getValue();

        switch (checkUnit.getType()){
            case URL:
            case DOMAIN:
            case IP_V4:
            case IP_V6:
                value = (value != null && !value.isEmpty() && !value.startsWith("http") ? "http://" + value : value);
                break;
            default:
                break;
        }
        return value;
    }

    private static void waitForTab(WebDriver webDriver, int tabIndex) { // one-based tab index
        new WebDriverWait(webDriver, WAIT_TIMEOUT)
                .until(driver -> driver != null &&
                        driver.getWindowHandles().size() > tabIndex - 1);
    }

    private static void switchToTab(WebDriver webDriver, int tabIndex) { // one-based tab index
        ArrayList<String> handles = new ArrayList<>(webDriver.getWindowHandles());
        webDriver.switchTo().window(handles.get(tabIndex - 1));
    }

    @Nullable
    public static WebElement findElementIfExists(By by, WebElement where) {
        try {
            List<WebElement> list = where.findElements(by);
            return list != null && list.size() > 0 ? list.get(0) : null;
        } catch (TimeoutException e) {
            // element not in the DOM
            return null;
        }
    }

    @Nullable
    public static WebElement findElementIfExists(By by, WebDriver driver) {
        try {
            List<WebElement> list = driver.findElements(by);
            return list != null && list.size() > 0 ? list.get(0) : null;
        } catch (TimeoutException e) {
            // element not in the DOM
            return null;
        }
    }

    @Nullable
    public static WebElement getElementBy(By by, WebDriver driver) {
        return ScriptUtils.findElementIfExists(by, driver);
    }

    public static String getTextOrDefault(WebElement element, String defaultValue) {
        try {
            return element == null ?
                    defaultValue : element.getText();
        } catch (StaleElementReferenceException e) {
            // ignore
        }
        return defaultValue;
    }

    public static void tryRemoveElementById(WebDriver driver, String id) {
        try {
            if (driver instanceof JavascriptExecutor) {
                ((JavascriptExecutor) driver).executeScript(
                        "return document.getElementById('" +
                                id + "').remove();");
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @Nullable
    public static String getPlainErrorDescriptionIfOccurred(WebDriver driver) {
        if (driver.getPageSource().length() <= ERROR_PAGE_SIZE_THRESHOLD) {
            WebElement element = findElementIfExists(By.xpath("//h1"), driver);
            String text = getTextOrDefault(element, null);
            if (text != null) {
                Pattern pattern = Pattern.compile("[1-5][0-9]{2}");
                Matcher matcher = pattern.matcher(text);
                return matcher.find() ? text : null;
            }
        }
        return null;
    }

    public static void type(WebElement element, long sleep, String query) {
        if (sleep > 0L) {
            query.codePoints().forEach(cp -> {
                element.sendKeys(new String(
                        Character.toChars(cp)));
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else {
            element.sendKeys(query);
        }
    }

    public static void scrollToBottom(JavascriptExecutor jsExecutor) {
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }


    // дикие эксперименты


    public static WebElement waitClickable(WebDriver driver, By locator, int timeoutInSeconds) {
        return new WebDriverWait(driver, timeoutInSeconds)
                .withMessage("clickable " + locator)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    @Nullable
    public static WebElement actOnStaleRef(WebElement element,
                                     Consumer<WebElement> action,
                                     Supplier<WebElement> refresher) {
        try {
            try {
                action.accept(element);
            } catch (StaleElementReferenceException e) {
                element = refresher.get();
                if (element != null)
                    // still may throw exception
                    action.accept(element);
            }
        } catch (TimeoutException e) {
            // ignore
        }
        return element;
    }

    public static void openScreenshotExtension(WebDriver driver){
        ((JavascriptExecutor)driver).executeScript("window.open()");
        switchToTab(driver, 2);
        driver.get(ChromeSettings.getScreenshotExtension().getPopupUrl());
        switchToTab(driver, 1);
    }

    public static String getScriptFromResource(String resourceName){
        try(InputStream inputStream = ScriptUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch(IOException ex){
            throw new RuntimeException("Ошибка! Скрипт " + resourceName + " не найден в ресурсах сервиса", ex);
        }
    }
}
