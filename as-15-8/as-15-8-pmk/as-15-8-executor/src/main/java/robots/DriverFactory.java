package robots;

import common.ExecutorProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import robots.utils.ScriptUtils;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Фабрика создания selenium драйвера
 * @author shabalinAI
 *
 */
@Slf4j
public class DriverFactory {

	/**
	 * Метод создания selenium драйвера
	 * @param hubURL URL selenium хаба
	 * @param platformName Имя платформы
	 * @param browserName Имя браузера
	 * @param version Версия приложения (ПС/ПАСД)
	 * @return
	 */
	public static WebDriver createDriver(URL hubURL, Platform platformName, String browserName, String version) {
		return createDriver(hubURL, platformName, browserName, version, null, false);
	}

	/**
	 * Метод создания selenium драйвера
	 * @param hubURL URL selenium хаба
	 * @param platformName Имя платформы
	 * @param browserName Имя браузера
	 * @param version Версия приложения (ПС/ПАСД)
	 * @param proxy прокси
	 * @return
	 */
	public static WebDriver createDriver(
			URL hubURL,
			Platform platformName,
			String browserName,
			String version,
			String proxy,
			boolean enableLog) {
		DesiredCapabilities cpb = buildCapability(platformName, browserName, version);

		Proxy oProxy = ProxyUtils.getSeleniumProxy(proxy);
		if (oProxy != null) {
			log.info("Create WebDriver, proxy: " + proxy);
			cpb.setCapability(CapabilityType.PROXY, oProxy);
		}
		else {
			log.info("Create WebDriver, proxy: NONE");
		}

		ChromeOptions options = new ChromeOptions();
		setLoadExtensions(options, Collections.singletonList(ChromeSettings.getScreenshotExtension()));
		setOptimalChromeOptions(options);
        setChromeAnonimyzerParams(options);

		if (enableLog){
			LoggingPreferences logPrefs = new LoggingPreferences();
			logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
			options.setCapability("goog:loggingPrefs", logPrefs);
			cpb.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
		}

		cpb.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new RemoteWebDriver(hubURL, cpb);
		ScriptUtils.openScreenshotExtension(driver);
		return driver;
	}

	/**
	 * Метод создания selenium драйвера Chrome с кастомным профилем
	 * @param hubURL URL selenium хаба
	 * @param platformName Имя платформы
	 * @param version Имя приложения (ПС/ПАСД)
	 * @param extensions расширения
	 * @return
	 */
	public static WebDriver createChromeDriver(URL hubURL, Platform platformName, String version,
											   List<ChromeSettings.Extension> extensions) {
		DesiredCapabilities cpb = buildCapability(
				platformName, "chrome", version);

        ChromeOptions options = new ChromeOptions();
		setOptimalChromeOptions(options);
		setLoadExtensions(options, extensions);
		cpb.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new RemoteWebDriver(hubURL, cpb);
		ScriptUtils.openScreenshotExtension(driver);
		return driver;
    }

    private static void setOptimalChromeOptions(ChromeOptions options){
		options.addArguments("--start-maximized");
		options.addArguments("--ignore-certificate-errors");
		//options.addArguments("--disable-popup-blocking");
		//options.addArguments("--headless");
		//options.addArguments("--window-size=1920,1080");
		//options.addArguments("--no-sandbox");				// избавляет от некоторых проблем с таймаутом, рендерингом, но не безопасно!
		options.addArguments("--dns-prefetch-disable");		// отключение предварительной выборки DNS. в теории должно ускорить работу
		//options.addArguments("--disable-gpu");				// говорят частично решает проблему с рендерингом (скриншотом)
		//options.addArguments("--disable-features=VizDisplayCompositor");	// у кого-то в 73 версии устранило проблему: Timed out receiving message from renderer: 10.000
		//options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable( LogType.PERFORMANCE, Level.ALL );
		options.setCapability( "goog:loggingPrefs", logPrefs );

		options.addArguments("--user-data-dir=" + ExecutorProperties.getChromeProperties().getUserDataDir());
		options.addArguments("--profile-directory=" + ExecutorProperties.getChromeProperties().getProfileName());
		options.addArguments("--auto-select-desktop-capture-source=Entire screen");

		//Для отображения полного URL
		options.addArguments("--enable-experimental-web-platform-features");
		options.addArguments("--enable-features=TemporaryUnexpireFlagsM76");
		options.addArguments("--disable-features=OmniboxUIExperimentHideSteadyStateUrlScheme,OmniboxUIExperimentHideSteadyStateUrlTrivialSubdomains");
	}

	private static void setChromeAnonimyzerParams(ChromeOptions options){
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
    }

	/**
	 * Метод создания параметров драйвера
	 * @param platform Имя платформы
	 * @param browserName Имя браузера
	 * @param version Имя приложения (ПС/ПАСД)
	 * @return
	 */
	private static DesiredCapabilities buildCapability(Platform platform, String browserName, String version) {
		DesiredCapabilities capability = createCapabilities(browserName);
		capability.setBrowserName(browserName);
		if(platform != null)
			capability.setPlatform(platform);
		else
			capability.setPlatform(Platform.ANY);
		if(Strings.isNotEmpty(version)) {
			capability.setVersion(version);
		}
		capability.setCapability("enableVNC", true);
		return capability;
	}

	private static void setLoadExtensions(ChromeOptions options, List<ChromeSettings.Extension> extensions) {
		options.addArguments("--load-extension=" + ChromeSettings.buildLoadExtensionArgValue(extensions));
	}

	/**
	 * Метод создания параметров драйвера
	 * @param browserName Имя браузера
	 * @return
	 */
	private static DesiredCapabilities createCapabilities(String browserName) {
		switch(browserName.toLowerCase().trim()) {
			case "chrome":
				return DesiredCapabilities.chrome();
			case "firefox":
				return DesiredCapabilities.firefox();
			case "edge":
				return DesiredCapabilities.edge();
			case "internetexplorer":
				return DesiredCapabilities.internetExplorer();
			case "opera":
				return DesiredCapabilities.operaBlink();
			case "safary":
				return DesiredCapabilities.safari();
			default:
				throw new IllegalArgumentException("Ошибка: браузер не поддерживается: "+browserName);
		}
	}
}
