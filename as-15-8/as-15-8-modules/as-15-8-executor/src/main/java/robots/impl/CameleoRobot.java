package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.CloudflareUtils;
import robots.utils.ScriptUtils;

import java.util.Map;

@Slf4j
public class CameleoRobot extends AnonymizerRobot {

	private static final String CAMELEO_URL = "http://www.cameleo.xyz";
	
	public CameleoRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams)  {
		super(driverParams, scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        driver.get(CAMELEO_URL);

        try {
            ScriptUtils.waitPageLoading(driver);
            WebElement input = driver.findElement(By.id("url"));
            input.sendKeys(checkUnit.getValue());
            driver.findElement(By.xpath("//*[@id=\"proxy\"]/div/div[2]/input")).click();
            ScriptUtils.waitPageLoading(driver);

            CloudflareUtils.waitCloudflareRedirect(driver);
            ScriptUtils.waitPageLoading(driver);
            if (CloudflareUtils.isCloudflareError(driver)) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(driver));
            }

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
                    "Не удалось найти элементы навигации CameleoXYZ", e);
        } catch (InterruptedException e) {
            throw new RobotScriptExecutionException(
                    "Выполнение потока прервано", e);
        }
    }
}
