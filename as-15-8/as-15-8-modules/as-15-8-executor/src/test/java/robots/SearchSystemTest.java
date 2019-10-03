package robots;

import com.fasterxml.jackson.databind.ObjectMapper;
import enums.AccessToolParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import robots.impl.CommonDirectSearchRobot;
import robots.utils.ScriptUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static enums.AccessToolParameters.*;

@RunWith(JUnit4.class)
public class SearchSystemTest {
    
    public static int LIMIT = 20;

    public static Map<AccessToolParameters, String> googleParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://www.google.ru");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@name=\"q\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "//form[@id=\"captcha-form\"]");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//*[@id=\"pnnext\"]");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@class=\"g\"]//div[@class=\"r\"]/a[1]");
        return params;
    }

    public static Map<AccessToolParameters, String> yandexParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://yandex.ru");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@name=\"text\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "//form[@action=\"/checkcaptcha\"]");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//div[contains(@class, \"pager \")]/a[last()]");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//*[@class=\"serp-item\"]//div[contains(@class, \"organic__path\")]/a[last()]");
        return params;
    }

    public static Map<AccessToolParameters, String> bingParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://www.bing.com/");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@id=\"sb_form_q\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//ol[@id=\"b_results\"]/li[@class=\"b_pag\"]/nav/ul/li[last()]/a");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//ol[@id=\"b_results\"]/li//h2/a");
        return params;
    }

    public static Map<AccessToolParameters, String> yahooParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://www.yahoo.com/");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@id=\"uh-search-box\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//div[@class=\"compPagination\"]/a[@class=\"next\"]");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//ol/li//h3/a");
        return params;
    }

    public static Map<AccessToolParameters, String> sputnikParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://www.sputnik.ru/");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//*[@id=\"js-search-input\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, ""); //
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//div[@class=\"b-paging\"]/a[last()]");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@class=\"b-result-list js-result-list\"]/div/div[@class=\"b-result-title\"]/a");
        return params;
    }

    public static Map<AccessToolParameters, String> mailParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://mail.ru/");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//*[@id=\"q\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//section[@id=\"js-bottomBar\"]//li[last()]/a");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//ul[@id=\"js-result\"]/li/div[1]/div/span/a");
        return params;
    }

    public static Map<AccessToolParameters, String> duckParams() {
        Map<AccessToolParameters, String> params = new HashMap<>();
        params.put(SEARCH_SYSTEM_URL, "https://duckduckgo.com");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "continuous");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@id=\"search_form_input_homepage\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//div[@id=\"links\"]/div[contains(@class, \"result--more\")]/a");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@id=\"links\"]/div[starts-with(@id, \"r1-\")]//a[@class=\"result__a\"]");
        return params;
    }

    @Test
    public void printLinks() throws InterruptedException {
        Map<AccessToolParameters, String> params = mailParams();
        String xpathInputField = params.get(SEARCH_SYSTEM_XPATH_INPUT_FIELD);
        String xpathCaptcha = params.get(SEARCH_SYSTEM_XPATH_CAPTCHA);
        String xpathNext = params.get(SEARCH_SYSTEM_XPATH_NEXT_PAGE);
        String xpathLink = params.get(SEARCH_SYSTEM_XPATH_ITEM_LINK);
        CommonDirectSearchRobot.ResultPageType type = CommonDirectSearchRobot.ResultPageType
                .valueOf(params.get(SEARCH_SYSTEM_RESULT_PAGE_TYPE).toUpperCase());

        ChromeDriver driver = createDriver(false);
        try {
            driver.get(params.get(SEARCH_SYSTEM_URL));

            WebElement inputField = driver.findElement(
                    By.xpath(xpathInputField));
            inputField.sendKeys("lingua");
            inputField.sendKeys(Keys.ENTER);

            if (xpathCaptcha.length() > 0 &&
                    driver.findElements(By.xpath(xpathCaptcha)).size() > 0)
                System.out.println("CAPTCHA");

            if (type == CommonDirectSearchRobot.ResultPageType.PAGINATION)
                printPaginatedLinksList(driver, xpathNext, xpathLink);
            else
                printContinuousLinksList(driver, xpathNext, xpathLink);

        } finally {
            Thread.sleep(20000);
            driver.quit();
        }
    }

    public void printPaginatedLinksList(ChromeDriver driver, String xpathNext, String xpathLink) {
        int counter = 0;
        boolean nextPage;

        do {

            counter += new WebDriverWait(driver, 10)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xpathLink)))
                    .stream()
                    .filter(Objects::nonNull)
                    .map(a -> a.getAttribute("href"))
                    .peek(System.out::println)
                    .count();

            System.out.println("\n" + counter + "\n");
            ScriptUtils.waitDriver(driver, 20);
            ScriptUtils.scrollToBottom(driver);

            nextPage = driver
                    .findElements(By.xpath(xpathNext))
                    .stream()
                    .limit(1)
                    .peek(WebElement::click)
                    .count() > 0;

            ScriptUtils.waitPageLoading(driver);
        } while (counter < LIMIT && nextPage);
    }

    public void printContinuousLinksList(ChromeDriver driver, String xpathNext, String xpathLink) {
        By linkLocator = By.xpath(xpathLink);

        int initResultCount = driver.findElements(linkLocator).size();
        System.out.println("init count: " + initResultCount);
        int buttonClickCount = (LIMIT - 1) / initResultCount;
        System.out.println("click count: " + buttonClickCount);

        ScriptUtils.scrollToBottom(driver);
        for (int i = 0; i < buttonClickCount && nextPage(driver, xpathNext); i++) {
            ScriptUtils.scrollToBottom(driver);
            ScriptUtils.waitPageLoading(driver);
        }

        List<WebElement> links = driver.findElements(linkLocator);
        while(links.size() < LIMIT && nextPage(driver, xpathNext)) {
            ScriptUtils.scrollToBottom(driver);
            ScriptUtils.waitPageLoading(driver);
            links = driver.findElements(linkLocator);
        }

        for (WebElement link : links) {
            System.out.println(link.getAttribute("href"));
        }
    }

    private boolean nextPage(ChromeDriver driver, String xpathNextPage) {
        WebElement next = ScriptUtils.findElementIfExists(
                By.xpath(xpathNextPage), driver);
        if (next != null) {
            next.click();
            return true;
        }
        return false;
    }

    @Test
    public void testLog() throws IOException {
        ChromeDriver driver = createDriver(false);
        driver.manage().timeouts()
                .implicitlyWait(1L, TimeUnit.SECONDS);
        try {
            driver.get("https://www.guru99.com/");

            String xpath = "//*[@id=\"java_technologies\"]/li[1]/a";
            WebElement element = driver.findElement(By.xpath(xpath));
            new Actions(driver)
                    .moveToElement(element, 0, 0)
                    .pause(5)
                    .click()
                    .build()
                    .perform();

            ScriptUtils.waitDriver(driver, 2);

            logsToFile(driver, "selenium_driver.json", LogType.DRIVER);
            logsToFile(driver, "selenium_browser.json", LogType.BROWSER);
            //logsToFile(driver, "selenium_performance.json", LogType.PERFORMANCE);
            logsToFile(driver, "selenium_client.json", LogType.CLIENT);
        } finally {
            driver.close();
        }
    }

    private ChromeDriver createDriver(boolean headless) {
        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\jspirina\\Projects\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(headless);
        options.addArguments("--start-maximized");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-popup-blocking");

        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.CLIENT, Level.ALL);
        logs.enable(LogType.DRIVER, Level.INFO);
        logs.enable(LogType.PERFORMANCE, Level.INFO);
        //logs.enable(LogType.PROFILER, Level.ALL); // not found
        //logs.enable(LogType.SERVER, Level.ALL); // not found
        options.setCapability(CapabilityType.LOGGING_PREFS, logs);
        //options.setCapability("goog:loggingPrefs", logs );

        return new ChromeDriver(options);
    }

    private void logsToFile(WebDriver driver, String filename,
                            String logType) throws IOException {
        LogEntries logs = driver.manage().logs().get(logType);

        String log = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(logs.getAll());

        File logFile = new File(filename);
        logFile.delete();
        try (FileOutputStream fos = new FileOutputStream(logFile)) {
            fos.write(log.getBytes());
        }
    }

}
