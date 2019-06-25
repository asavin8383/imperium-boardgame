package robots.impl;

import static enums.AccessToolParameters.INPUT_DELAY;
import static robots.utils.ScriptUtils.findElementIfExists;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import enums.AccessToolParameters;
import execution.ExecutionPSJobResult;
import robots.RobotDriverParameters;
import robots.exceptions.Captcha_RobotScriptExecutionException;
import robots.exceptions.RobotScriptExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

public abstract class SearchRobot extends SeleniumRobot {

	/** Максимальное кол-во проверяемых результатов по умолчанию */
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    /** Задержка при вводе по умолчанию (мс) */
    private static final long DEFAULT_INPUT_DELAY = 0;

    /** Максимальное кол-во проверяемых результатов */
    private int searchResultLimit;

    /** Текущее кол-во проверенных результатов */
    private int checkedResultCount;

    /** Задержка при вводе (мс) */
    private long inputDelay;
    
	public SearchRobot(RobotDriverParameters driverParams,
                        Map<AccessToolParameters, String> scriptParams,
                        int searchLimit) {
		
		super(driverParams, scriptParams);
		this.checkedResultCount = 0;

		this.searchResultLimit = searchLimit > 0 ? searchLimit : DEFAULT_SEARCH_LIMIT;

		long delayParameterValue = scriptParams.containsKey(INPUT_DELAY) ?
                Long.parseLong(scriptParams.get(INPUT_DELAY)) : DEFAULT_INPUT_DELAY;
		this.inputDelay = delayParameterValue < 0 ?
                DEFAULT_INPUT_DELAY : delayParameterValue;
	}

	ExecutionPSJobResult checkSearchResult(EqualityTest test) throws RobotScriptExecutionException {
		try {
			do {
				if (captcha())
					throw new Captcha_RobotScriptExecutionException("Обнаружена captcha");

				List<WebElement> elements = collectLinkElements();

				for (WebElement element : elements) {
					String href = element.getAttribute("href");
					// log.info(checkedResultCount + ". " + href);

					if (test.equalTo(href)) {
						scrollTo(element);
						return createExecutionResult(true);
					}

					++checkedResultCount;
				}

			} while (checkedResultCount < searchResultLimit && nextPage());

			return createExecutionResult(false);
		} catch (Exception ex) {
			throw new RobotScriptExecutionException(ex);
		}
	}

    ExecutionPSJobResult createExecutionResult(boolean linkFound) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setLinkFound(linkFound);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));   // в любом случае делаем скриншот
        return message;
    }

    void input(WebElement element, String query) {
        SearchRobot.input(element, inputDelay, query);
    }

    private boolean nextPage() throws TimeoutScriptException {
        WebElement next = findElementIfExists(nextPageBy(), driver);
        try {
            if (next != null) {
                next.click();
                ScriptUtils.waitPageLoading(driver);
                return true;
            }
        } catch (TimeoutException e) {
            throw new TimeoutScriptException(e);
        }
        return false;

    }

    private void scrollTo(WebElement webElement) {
        Actions actions = new Actions(driver);
        actions.moveToElement(webElement, 0, 0).perform();
    }

    private static void type(WebElement element, long sleep, String query) {
        query.codePoints().forEach(cp -> {
            element.sendKeys(new String(
                    Character.toChars(cp)));
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void input(WebElement element, long sleep, String query) {
        if (sleep == 0L)
            element.sendKeys(query);
        else
            type(element, sleep, query);
    }



    protected abstract By nextPageBy();

    protected abstract boolean captcha();

    protected abstract List<WebElement> collectLinkElements();

}
