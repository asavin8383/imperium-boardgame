package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.util.StringUtils;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static robots.utils.ScriptUtils.findElementIfExists;

/**
 * Скрипт проверки ПС Google
 * @author shabalinAI
 *
 */
@Slf4j(topic = "robots")
public class GoogleRobot extends SearchRobot {

	private static final String GOOGLE_URL = "https://www.google.ru";

	public GoogleRobot(RobotDriverParameters driverParams,
						Map<AccessToolParameters, String> scriptParams,
						int searchLimit) {
		super(driverParams, scriptParams, searchLimit);
	}
	
	@Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
		EqualityTest test = EqualityTest.forCheckUnit(checkUnit);
		
		driver.get(GOOGLE_URL);

		WebElement input = driver.findElement(By.name("q"));
		input(input, checkUnit.getValue() + " ");
		input.sendKeys(Keys.ENTER);

		return createMessage(checkSearchResult(test));
	}

	@Override
	protected By nextPageLocator() {
		return By.id("pnnext");
	}

	@Override
	protected boolean captcha() {
		try {
			String strUrl = ScriptUtils.getCurrentUrl(driver);

			if (StringUtils.isEmpty(strUrl))
				return false;

			return new URL(strUrl).getPath()
					.equals("/sorry/index");

		} catch (MalformedURLException e) {
			log.warn("Ошибка при проверке captcha Google: {}",
					e.getLocalizedMessage());
		}
		return false;
	}

	@Override
	protected boolean checkPageResult(EqualityTest test) {
		List<WebElement> links = collectLinkElements();
		Iterator<WebElement> it = links.iterator();
		while (it.hasNext() && withinLimit()) {
			WebElement link = it.next();
			String url = link.getAttribute("href");

			try {
				if (test.equalTo(url)) {
					scrollTo(link);
					return true;
				}
			} catch (MalformedURLException e) {
				log.warn("Ошибка при проверке ссылки '{}' Google: {}",
						url, e.getLocalizedMessage());
			}
		}
		return false;
	}

	private List<WebElement> collectLinkElements() {
		return driver.findElements(By.xpath("//div[@class=\"g\"]")).stream()
				.map(div -> findElementIfExists(By.xpath(".//a[1]"), div))
				.collect(Collectors.toList());
	}
}
