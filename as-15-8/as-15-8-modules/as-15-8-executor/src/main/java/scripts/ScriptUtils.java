package scripts;

import checkUnits.CheckUnit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ScriptUtils {

    private static final int ERROR_PAGE_SIZE_THRESHOLD = 2048;

    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageResult {
        public String pageSource;
        public String errorCodeChrome;
    }

    public static Integer WAIT_TIMEOUT = 30;
    public static Integer WAIT_DRIVER_DEFAULT = 3;


    public static void waitDriver(WebDriver driver){
        waitDriver(driver, WAIT_DRIVER_DEFAULT);
    }

    public static void waitDriver(WebDriver driver, Integer seconds){
        try{
            WebDriverWait wait = new WebDriverWait(driver, 3);
            wait.until(webDriver -> false);
        }
        catch (TimeoutException te){}
    }

    public static void waitPageLoading(WebDriver driver) {
        new WebDriverWait(driver, WAIT_TIMEOUT).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    public static String getErrorCode(WebDriver driver){
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

    /**
     * Параллельная загрузка исходника страниц.
     **/
    public static List<PageResult> getPageSources(List<WebDriver> drivers, String url){
        List<CompletableFuture<PageResult>> pageContentFutures = drivers.stream()
                .map(webDriver -> {
                    return CompletableFuture.supplyAsync(() -> {
                        PageResult pResult = new PageResult();
                        try{
                            webDriver.get(url);
                            webDriver.manage().window().fullscreen();
                            waitDriver(webDriver, 3);
                            pResult = getPageSource(webDriver);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        return pResult;
                    });
                })
                .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                pageContentFutures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<PageResult>> allPageContentsFuture = allFutures.thenApply(v -> {
            return pageContentFutures.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });

        List<PageResult> pageResults = allPageContentsFuture.join();
        return pageResults;
    }

    public static List<String> getEtalonProxies(Map<String, String> proxies){
        List<String> etalonProxies = new ArrayList<>();

        for (Map.Entry<String, String> entry : proxies.entrySet()) {
            String key = entry.getKey();
            String proxy = entry.getValue();
            if(key.toLowerCase().startsWith("etalon")){
                etalonProxies.add(proxy);
            }
        }
        return etalonProxies;
    }

    public static byte[] getScreenshot(WebDriver webDriver) {
        return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
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

    @Nullable
    public static WebElement findElementIfExists(By by, WebElement where) {
        List<WebElement> list = where.findElements(by);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    @Nullable
    public static WebElement findElementIfExists(By by, WebDriver driver) {
        List<WebElement> list = driver.findElements(by);
        return list != null && list.size() > 0 ? list.get(0) : null;
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
//        if (driver.getPageSource().length() <= ERROR_PAGE_SIZE_THRESHOLD) {
        WebElement element = findElementIfExists(By.xpath("//h1"), driver);
        String text = getTextOrDefault(element, null);
        if (text != null) {
            Pattern pattern = Pattern.compile("[1-5][0-9]{2}");
            Matcher matcher = pattern.matcher(text);
            return matcher.find() ? text : null;
        }
//        }
        return null;
    }

}
