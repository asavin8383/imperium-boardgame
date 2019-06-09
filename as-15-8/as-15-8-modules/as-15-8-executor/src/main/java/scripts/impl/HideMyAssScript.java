package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;

import java.util.Map;

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
            if (captcha()) {
                throw new Captcha_RobotScriptExecutionException(
                        "Обнаружена captcha-form на HideMyAss");
            }
        } catch (TimeoutException e) {
            return getTimeoutMessage();
        } catch (NoSuchElementException e) {
            throw new RobotScriptExecutionException(
                    "Не удалось найти элементы навигации HideMyAss", e);
        }

        return process(checkUnit);
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
}
