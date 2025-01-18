package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import lombok.Getter;
import lombok.Setter;
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

    @Getter
    @Setter
    private int restartAttempts;

    @Getter
    private final int restartInterval;
	
	public CameleoRobot(Map<AccessToolParameter, String> scriptParams)  {
		super(scriptParams);
        this.restartAttempts = Integer.parseInt(scriptParams.getOrDefault(AccessToolParameter.RESTART_ATTEMPTS, "0"));
        this.restartInterval = Integer.parseInt(scriptParams.getOrDefault(AccessToolParameter.RESTART_INTERVAL, "0"));
	}

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException {
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
            throw new ExecutionException(
                    "Не удалось найти элементы навигации CameleoXYZ", e);
        } catch (InterruptedException e) {
            throw new ExecutionException(
                    "Выполнение потока прервано", e);
        }
    }

}
