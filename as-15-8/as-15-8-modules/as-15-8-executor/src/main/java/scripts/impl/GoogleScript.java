package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import scripts.ScriptDriverParameters;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.utils.EqualityTest;
import scripts.utils.ScriptUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static scripts.utils.ScriptUtils.findElementIfExists;

/**
 * Скрипт проверки ПС Google
 * @author shabalinAI
 *
 */
public class GoogleScript extends SearchScript {

	private static final String GOOGLE_URL = "https://www.google.ru";

	public GoogleScript(ScriptDriverParameters driverParams,
						Map<AccessToolParameters, String> scriptParams,
						int searchLimit) {
		super(driverParams, scriptParams, searchLimit);
	}
	
	@Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException{
		EqualityTest test = EqualityTest.forCheckUnit(checkUnit);
		
		driver.get(GOOGLE_URL);

		WebElement input = driver.findElement(By.name("q"));
		input(input, checkUnit.getValue() + " ");
		input.sendKeys(Keys.ENTER);

		return checkSearchResult(test);
	}

	@Override
	protected By nextPageBy() {
		return By.id("pnnext");
	}

	@Override
	protected boolean captcha() {
		try {
			URI uri = new URI(ScriptUtils.getCurrentUrl(driver));
			return uri.getPath().equals("/sorry/index");
		} catch (URISyntaxException e) {
			// ignore
		}
		return false;
	}

	@Override
	protected List<WebElement> collectLinkElements() {
		return driver.findElements(By.xpath("//div[@class=\"g\"]")).stream()
				.map(div -> findElementIfExists(By.xpath(".//a[1]"), div))
				.collect(Collectors.toList());
	}
}
