package scripts;

import checkUnits.CheckUnit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class ScriptUtils {

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

    public static void type(WebElement input, long sleep, String query) {
        query.codePoints().forEach(cp -> {
            input.sendKeys(new String(
                    Character.toChars(cp)));
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
