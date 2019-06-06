package scripts.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutScriptException;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class HideMyAssScript extends AnonymizerScript {

    private static final String URL = "https://www.hidemyass.com/proxy";

    @Override
    public void execute() throws RobotScriptExecutionException {
        driver.get(URL);
        driver.manage().window().fullscreen();
        driver.switchTo().frame(driver.findElement(By.id("proxyIframe")));

        // to do no such element
        WebElement input = driver.findElement(By.id("form_url"));
        //input.sendKeys(getCheckUnit().getValue());
        ScriptUtils.type(input, getInputDelay(),
                getCheckUnit().getValue());

        try {
            new WebDriverWait(driver, 10)
                    .until(presenceOfElementLocated(
                            By.xpath("/html/body/form/div[3]/a")))
                    .click();
        } catch (TimeoutException e) {
            throw new TimeoutScriptException(e);
        }

        super.execute();
    }

    @Override
    protected boolean captcha() {
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
