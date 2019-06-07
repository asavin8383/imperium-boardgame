package scripts.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutScriptException;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.net.MalformedURLException;
import java.util.Map;

public class CameleoScript extends AnonymizerScript {

	private static final String CAMELEO_URL = "http://www.cameleo.xyz";
	
	public CameleoScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, Long inputDelay) throws MalformedURLException {
		super(driverParams, scriptParams, inputDelay);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        driver.get(CAMELEO_URL);
        driver.manage().window().fullscreen();

        WebElement input = driver.findElement(By.id("url"));
        //input.sendKeys(getCheckUnit().getValue());
        ScriptUtils.type(input, getInputDelay(),
                checkUnit.getValue());

        try {
            new WebDriverWait(driver, 10)
                    .until(presenceOfElementLocated(
                            By.xpath("//*[@id=\"proxy\"]/div/div[2]/input")))
                    .click();
        } catch (TimeoutException e) {
            throw new TimeoutScriptException(e);
        }

        return super.execute(checkUnit);
    }

    @Override
    protected boolean captcha() {
        return false;
    }
}
