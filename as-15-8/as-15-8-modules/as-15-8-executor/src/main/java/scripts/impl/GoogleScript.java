package scripts.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import scripts.ScriptDriverParameters;
import scripts.ScriptUtils;
import scripts.exceptions.RobotScriptExecutionException;

/**
 * Скрипт проверки ПС Google
 * @author shabalinAI
 *
 */
public class GoogleScript extends SearchScript {

	private static final String GOOGLE_URL = "https://www.google.ru";

	public GoogleScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, int searchLimit, long inputDelay) throws MalformedURLException {
		super(driverParams, scriptParams, searchLimit, inputDelay);
	}
	
	@Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException{
		EqualityTest test = EqualityTest.forCheckUnit(checkUnit);
		
		driver.get(GOOGLE_URL);
		driver.manage().window().fullscreen();

		WebElement input = driver.findElement(By.name("q"));
		//input.sendKeys(getCheckUnit().getValue());
		ScriptUtils.type(input, getInputDelay(),
				checkUnit.getValue() + " ");
		input.sendKeys(Keys.ENTER);

		return checkSearchResult(test);
	}

	@Override
	protected boolean nextPage() {
		return nextPage(By.id("pnnext"));
	}

	@Override
	protected boolean captcha() {
		try {
			URI uri = new URI(driver.getCurrentUrl());
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
