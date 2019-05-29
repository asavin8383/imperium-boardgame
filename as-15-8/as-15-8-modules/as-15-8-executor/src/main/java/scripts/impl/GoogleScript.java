package scripts.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import scripts.RobotScriptExecutionException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Скрипт проверки ПС Google
 * @author shabalinAI
 *
 */
public class GoogleScript extends SearchScript {

	private static final String GOOGLE_URL = "https://www.google.ru";

	@Override
	public void execute() throws RobotScriptExecutionException{
		driver.get(GOOGLE_URL);
		driver.manage().window().fullscreen();

		WebElement input = driver.findElement(By.name("q"));
		input.sendKeys(getCheckUnit().getValue());
		input.sendKeys(Keys.ENTER);

		sendExecutionResult(checkSearchResult());
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
