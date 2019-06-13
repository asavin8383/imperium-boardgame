package scripts;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import scripts.exceptions.TimeoutScriptException;

import static scripts.ScriptUtils.findElementIfExists;
import static scripts.ScriptUtils.getTextOrDefault;

@UtilityClass
public class CloudflareUtils {

    public static boolean isCloudflareError(WebDriver driver) {
        return findElementIfExists(By.xpath("//*[@id=\"cf-error-details\"]"), driver) != null;
    }

    public static String getCloudflareErrorDetails(WebDriver driver) {
        WebElement errorElement = findElementIfExists(
                By.xpath("//*[@id=\"cf-error-details\"]"), driver);
        if (errorElement != null) {
            WebElement codeElement = findElementIfExists(
                    By.xpath("//span[contains(@class, \"cf-error-code\")]"), errorElement);
            WebElement descriptionElement = findElementIfExists(
                    By.xpath("//h2[contains(@class, \"cf-subheadline\")]"), errorElement);
            return String.format("Error %s: %s",
                    getTextOrDefault(codeElement, "<no code>"),
                    getTextOrDefault(descriptionElement, "<no description>"));
        }
        return "Элемент cf-error-details не найден";
    }

    public static boolean isCloudflareDdosProtection(WebDriver driver) {

        WebElement cloudflareLink = findElementIfExists(
                By.xpath("//div[contains(@class, \"attribution\")]//a"), driver);
        if (cloudflareLink != null)
            return ScriptUtils.getTextOrDefault(cloudflareLink, "")
                    .equals("DDoS protection by Cloudflare");


        return false;
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
            throw new TimeoutScriptException(
                    "DDoS protection by Cloudflare. " +
                            "Время ожидания редиректа (" +
                            timeMax / 1000 +
                            " секунд) истекло");
    }
}
