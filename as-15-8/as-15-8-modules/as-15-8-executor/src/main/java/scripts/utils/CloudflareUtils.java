package scripts.utils;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scripts.exceptions.TimeoutCheckingBrowserException;
import scripts.exceptions.TimeoutScriptException;

import static scripts.utils.ScriptUtils.findElementIfExists;
import static scripts.utils.ScriptUtils.getTextOrDefault;

@UtilityClass
public class CloudflareUtils {

    public static final String CHECK_BROWSE_ERROR = "CHECK_BROWSE_ERROR";

    public static boolean isCloudflareError(WebDriver driver) {
        return findElementIfExists(By.xpath("//*[@id=\"cf-error-details\"]"), driver) != null;
    }

    public static String getCloudflareErrorDetails(WebDriver driver) {
        return getCloudflareErrorDetailsOpt(driver, "Элемент cf-error-details не найден");
    }

    public static String getCloudflareErrorDetailsOpt(WebDriver driver, String opt) {
        WebElement errorElement = findElementIfExists(
                By.xpath("//*[@id=\"cf-error-details\"]"), driver);
        if (errorElement != null) {
            WebElement codeElement = findElementIfExists(
                    By.xpath("//span[contains(@class, \"cf-error-code\")]"), errorElement);
            WebElement descriptionElement = findElementIfExists(
                    By.xpath("//h2[contains(@class, \"cf-subheadline\")]"), errorElement);
            return String.format("%s %s: %s",
                    CHECK_BROWSE_ERROR,
                    getTextOrDefault(codeElement, "<no code>"),
                    getTextOrDefault(descriptionElement, "<no description>"));
        }
        return opt;
    }

    public static boolean isCloudflareDdosProtection(WebDriver driver) {

        WebElement cloudflareLink = findElementIfExists(
                By.cssSelector(".cf-browser-verification #cf-content1, .attribution"), driver);

        if (cloudflareLink != null){
            String text = ScriptUtils.getTextOrDefault(cloudflareLink, "").toLowerCase();
            return (text.contains("check") && text.contains("browser")) ||
                    (text.contains("ddos") && text.contains("cloudflare"));
        }
        return false;
    }

    public static void waitCloudflareRedirect(WebDriver driver, int timeoutMs)
            throws InterruptedException, TimeoutScriptException {

        waitCloudflareRedirect(driver, (timeoutMs > 10000 ? 5000 : timeoutMs / 2), timeoutMs);
    }

    public static void waitCloudflareRedirect(WebDriver driver)
            throws InterruptedException, TimeoutScriptException {

        waitCloudflareRedirect(driver, 5 * 1000, 30 * 1000);
    }

    public static void waitCloudflareRedirect(WebDriver driver, long timeStep, long timeMax)
            throws InterruptedException, TimeoutScriptException {

        long total = 0;

        while (isCloudflareDdosProtection(driver) && total < timeMax) {
            total += timeStep;
            Thread.sleep(timeStep);
        }

        if (isCloudflareDdosProtection(driver))
            throw new TimeoutCheckingBrowserException(
                    "DDoS protection by Cloudflare. " +
                            "Время ожидания редиректа (" +
                            timeMax / 1000 +
                            " секунд) истекло");
    }

    public static void waitCloudflareRedirect(WebDriver driver, int timeoutInSeconds,
                                              boolean useDriverWait) throws TimeoutCheckingBrowserException {
        try {
            new WebDriverWait(driver, timeoutInSeconds)
                    .withMessage("DDoS protection by Cloudflare пройдена")
                    .until(ExpectedConditions.not(CloudflareUtils::isCloudflareDdosProtection));
        } catch (TimeoutException e) {
            throw new TimeoutCheckingBrowserException(
                    "DDoS protection by Cloudflare. " +
                            "Время ожидания редиректа (" +
                            timeoutInSeconds +
                            " секунд) истекло");
        }
    }
}
