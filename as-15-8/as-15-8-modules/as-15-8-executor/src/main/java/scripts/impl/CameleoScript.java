package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;

import java.util.Map;

@Slf4j
public class CameleoScript extends AnonymizerScript {

	private static final String CAMELEO_URL = "http://www.cameleo.xyz";
	
	public CameleoScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams)  {
		super(driverParams, scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        driver.get(CAMELEO_URL);
        driver.manage().window().fullscreen();

        try {
            ScriptUtils.waitPageLoading(driver);
            WebElement input = driver.findElement(By.id("url"));
            input.sendKeys(checkUnit.getValue());
            driver.findElement(By.xpath("//*[@id=\"proxy\"]/div/div[2]/input")).click();
            ScriptUtils.waitPageLoading(driver);
            ScriptUtils.waitCloudflareRedirect(driver);
            ScriptUtils.waitPageLoading(driver);

        } catch (TimeoutException e) {
            return getTimeoutMessage();
        }catch (NoSuchElementException e) {
            throw new RobotScriptExecutionException(
                    "Не удалось найти элементы навигации CameleoXYZ", e);
        } catch (InterruptedException e) {
            throw new RobotScriptExecutionException(
                    "Выполнение потока прервано", e);
        }

        return process(checkUnit);
    }
}
