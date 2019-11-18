package robots.utils;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.AccessToolParameter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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

    public static Integer WAIT_TIMEOUT = 30;
    public static Integer WAIT_DRIVER_DEFAULT = 3;


    public static void waitDriver(WebDriver driver){
        waitDriver(driver, WAIT_DRIVER_DEFAULT);
    }

    public static void waitDriver(WebDriver driver, Integer seconds){
        try{
            WebDriverWait wait = new WebDriverWait(driver, seconds);
            wait.until(webDriver -> false);
        }
        catch (TimeoutException te){}
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

    public static byte[] getScreenshot(WebDriver driver)  {
        try {
            try(InputStream inputStream = ScriptUtils.class.getClassLoader().getResourceAsStream("takeScreenshot.js")){
                String script = IOUtils.toString(inputStream, Charset.forName("UTF-8"));
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String imgBase64 = js.executeAsyncScript(script).toString();
                return Base64.getDecoder().decode(imgBase64);
            }
        } catch(Exception ex){
            throw new RuntimeException("Ошибка получения скриншота", ex);
        }
    }

    /*public static byte[] getScreenshot(WebDriver webDriver)  {
        try{

            ((JavascriptExecutor)webDriver).executeScript("window.open()");
            switchToTab(webDriver, 2);
            webDriver.get(ChromeSettings.getScreenshotExtension().getPopupUrl());
            WebDriverWait wait = new WebDriverWait(webDriver, 100000000);
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("desktop"))).click();

            ScriptUtils.waitForTab(webDriver, 2);
    //        wait.until(driver -> driver != null &&
    //                driver.getWindowHandles().size() < 2);
    //        ScriptUtils.waitForTab(webDriver, 2);

            switchToTab(webDriver, 2);
            WebElement frame = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id=\"iframe_container\"]/iframe")));
            webDriver.switchTo().frame(frame);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@id=\"container\"]")));

            waitDriver(webDriver, 1);

           /* String imgElemPath = "//div[contains(@class, \"editor-container\")]";
            WebElement imgElem = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.xpath(imgElemPath)))
                    .findElement(By.xpath(imgElemPath));*/

           // return getSeleniumScreenshot(webDriver, imgElem);
            /*BufferedImage originalImage =
                    new AShot().takeScreenshot(webDriver,
                            webDriver.findElement(By.xpath("//div[@id=\"container\"]"))).getImage();

            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(originalImage, "png", baos);
                baos.flush();
                return baos.toByteArray();
            }
            String base64Image = wait
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[@id=\"container\"]/img")))
                    .getAttribute("src").split(",")[1];

            // close screenshot tab
            webDriver.close();
            switchToTab(webDriver, 1);

            return Base64.getDecoder().decode(base64Image);
        }catch(Exception ex){
            throw new RuntimeException("Ошибка получения скриншота", ex);
        }
    }*/

    private static byte[] getSeleniumScreenshot(WebDriver driver, WebElement ele) throws IOException {
        File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        BufferedImage  fullImg = ImageIO.read(screenshot);

        Point point = ele.getLocation();

        int eleWidth = ele.getSize().getWidth();
        int eleHeight = ele.getSize().getHeight();

        BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), point.getY(),
                eleWidth, eleHeight);
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(eleScreenshot, "png", baos);
            baos.flush();
            return baos.toByteArray();
        }
    }

    public static byte[] getScreenshot(WebDriver webDriver, boolean old) {
        waitDriver(webDriver, 1);   // задержка скриншота, чтоб избежать белого экрана, т.к. некоторые сайты показывают контент не сразу.

        try {
            return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
        }
        catch (TimeoutException te){
            return null;        // вероятно, загрузка страницы была прервана по явному таймауту, поэтому DOM не сформировался.
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

    public static void waitForTab(WebDriver webDriver, int tabIndex) { // one-based tab index
        new WebDriverWait(webDriver, WAIT_TIMEOUT)
                .until(driver -> driver != null &&
                        driver.getWindowHandles().size() > tabIndex - 1);
    }

    public static void switchToTab(WebDriver webDriver, int tabIndex) { // one-based tab index
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
        WebElement element = ScriptUtils.findElementIfExists(by, driver);
        return element;
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

    public static boolean useEtalon(Map<AccessToolParameter, String> scriptParams){
        if (scriptParams == null)
            return true;

        String useEtalon = scriptParams.get(AccessToolParameter.USE_ETALON);
        return StringUtils.isEmpty(useEtalon) ||
                useEtalon.equalsIgnoreCase("true") ||
                useEtalon.equalsIgnoreCase("on") ||
                useEtalon.equalsIgnoreCase("1");
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

}
