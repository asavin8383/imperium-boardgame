package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import robots.exceptions.ExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.CloudflareUtils;
import robots.utils.ScriptUtils;

import java.util.Map;

@Slf4j
public class CameleoRobot extends AnonymizerRobot {

	private static final String CAMELEO_URL = "http://www.cameleo.xyz";
	
	public CameleoRobot(Map<AccessToolParameter, String> scriptParams)  {
		super(scriptParams);
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException, InterruptedException {
        getDriver().get(CAMELEO_URL);

        try {
            ScriptUtils.waitPageLoading(getDriver());
            WebElement input = getDriver().findElement(By.id("url"));
            input.sendKeys(checkUnit.getValue());
            getDriver().findElement(By.xpath("//*[@id=\"proxy\"]/div/div[2]/input")).click();
            ScriptUtils.waitPageLoading(getDriver());

            CloudflareUtils.waitCloudflareRedirect(getDriver());
            ScriptUtils.waitPageLoading(getDriver());
            if (CloudflareUtils.isCloudflareError(getDriver())) {
                return getErrorMessage(CloudflareUtils
                        .getCloudflareErrorDetails(getDriver()));
            }

            String plainError = ScriptUtils
                    .getPlainErrorDescriptionIfOccurred(getDriver());
            if (plainError != null)
                return getErrorMessage(plainError);

            return process(checkUnit);

        } catch (TimeoutException | TimeoutScriptException e) {
            log.info("TimeoutException при получении страницы", e);
            return getTimeoutMessage();
        } catch (NoSuchElementException e) {
            throw new ExecutionException(
                    "Не удалось найти элементы навигации CameleoXYZ", e);
        } catch (InterruptedException e) {
            throw new ExecutionException(
                    "Выполнение потока прервано", e);
        }
    }
}
