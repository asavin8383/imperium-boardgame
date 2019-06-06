package scripts.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutScriptException;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class CameleoScript extends AnonymizerScript {

    private static final String CAMELEO_URL = "http://www.cameleo.xyz";

    @Override
    public void execute() throws RobotScriptExecutionException {
        driver.get(CAMELEO_URL);
        driver.manage().window().fullscreen();

        WebElement input = driver.findElement(By.id("url"));
        //input.sendKeys(getCheckUnit().getValue());
        ScriptUtils.type(input, getInputDelay(),
                getCheckUnit().getValue());

        try {
            new WebDriverWait(driver, 10)
                    .until(presenceOfElementLocated(
                            By.xpath("//*[@id=\"proxy\"]/div/div[2]/input")))
                    .click();
        } catch (TimeoutException e) {
            throw new TimeoutScriptException(e);
        }

        super.execute();
    }
}
