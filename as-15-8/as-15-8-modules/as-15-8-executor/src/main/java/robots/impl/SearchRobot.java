package robots.impl;

import enums.AccessToolParameters;
import execution.ExecutionPSJobResult;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import robots.RobotDriverParameters;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.exceptions.TimeoutScriptException;
import robots.utils.EqualityTest;
import robots.utils.ScriptUtils;

import java.util.Map;

import static enums.AccessToolParameters.INPUT_DELAY;
import static robots.utils.ScriptUtils.findElementIfExists;

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

    protected abstract By nextPageLocator();

    protected abstract boolean captcha();

    protected abstract boolean checkPageResult(EqualityTest test);

	final void incCheckResultCount() {
        ++checkedResultCount;
    }

    final boolean withinLimit() {
	    return checkedResultCount < searchResultLimit;
    }

    final void input(WebElement element, String query) {
        SearchRobot.input(element, inputDelay, query);
    }

    final void scrollTo(WebElement webElement) {
        Actions actions = new Actions(driver);
        actions.moveToElement(webElement, 0, 0).perform();
    }

    boolean nextPage() throws TimeoutScriptException {
        WebElement next = findElementIfExists(
                nextPageLocator(), driver);
        try {
            if (next != null) {
                next.click(); //  not clickable ?
                ScriptUtils.waitPageLoading(driver);
                return true;
            }
        } catch (TimeoutException e) {
            throw new TimeoutScriptException(e);
        }
        return false;

    }

    boolean checkSearchResult(EqualityTest test)
            throws ExecutionException {
        do {
            if (captcha())
                throw new Captcha_ExecutionException(
                        "Обнаружена Captcha в Yandex");

            if (checkPageResult(test))
                return true;

        } while (withinLimit() && nextPage());

        return false;
    }

    final ExecutionPSJobResult createMessage(boolean linkFound) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setLinkFound(linkFound);
        message.setError(false);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }

    final ExecutionPSJobResult createErrorMessage(String errorDetails) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setLinkFound(false);
        message.setError(true);
        message.setErrorDetails(errorDetails);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
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

}
