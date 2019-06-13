package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import scripts.CloudflareUtils;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutScriptException;

import java.util.Map;

@Slf4j
public class HideMyAssScript extends AnonymizerScript {
	
	private static final String URL = "https://www.hidemyass.com/proxy";

    public HideMyAssScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		super(driverParams, scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException { 	 
        driver.get(URL);
        driver.manage().window().fullscreen();

        try {
            ScriptUtils.waitPageLoading(driver);
            driver.switchTo().frame(driver.findElement(By.id("proxyIframe")));
            WebElement input = driver.findElement(By.id("form_url"));
            input.sendKeys(checkUnit.getValue());
            driver.findElement(By.xpath("/html/body/form/div[3]/a")).click();
            ScriptUtils.waitPageLoading(driver);

            CloudflareUtils.waitCloudflareRedirect(driver);
            ScriptUtils.waitPageLoading(driver);
            if (CloudflareUtils.isCloudflareError(driver)) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(driver));
            }

            if (captcha())
                throw new Captcha_RobotScriptExecutionException(
                        "Обнаружена captcha-form на HideMyAss");

            if (isHideMyAssErrorPage())
                return getErrorMessage(getHideMyAssErrorDetails());

            // todo get final url before remove
            ScriptUtils.tryRemoveElementById(driver, "hma-top");

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(driver);
            if (plainError != null)
                return getErrorMessage(plainError);

            return process(checkUnit);

        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении страницы", e);
            return getTimeoutMessage();
        } catch (NoSuchElementException e) {
            throw new RobotScriptExecutionException(
                    "Не удалось найти элементы навигации HideMyAss", e);
        } catch (InterruptedException e) {
            throw new RobotScriptExecutionException(
                    "Выполнение потока прервано", e);
        }
    }

    private boolean captcha() {
        try {
            WebElement captchaForm = driver.findElement(
                    By.xpath("//*[@id=\"captcha-form\"]"));
            return captchaForm != null;
        } catch (NoSuchElementException e) {
            // ignore
        }
        return false;
    }

    private boolean isHideMyAssErrorPage() {
        WebElement headerElement = ScriptUtils.findElementIfExists(
                By.xpath("//*[@id=\"top\"]/div/h1"), driver);
        String text = ScriptUtils.getTextOrDefault(
                headerElement, null);
        return text != null && text.equalsIgnoreCase(
                "Oops! Something’s gone wrong...");
    }

    private String getHideMyAssErrorDetails() {
        WebElement element = ScriptUtils.findElementIfExists(
                By.xpath("//*[@id=\"top\"]/div/p"), driver);
        return ScriptUtils.getTextOrDefault(element,
                "Описание ошибки не найдено");
    }
}
