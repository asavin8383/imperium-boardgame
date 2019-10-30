package robots;

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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
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
	 * @param appName Имя приложения (ПС/ПАСД)
	 * @param browserName Имя браузера
	 * @return
	 */
	public static WebDriver createDriver(URL hubURL, Platform platformName, String appName, String browserName) {
		return createDriver(hubURL, platformName, appName, browserName, null, false);
	}

	/**
	 * Метод создания selenium драйвера
	 * @param hubURL URL selenium хаба
	 * @param platformName Имя платформы
	 * @param appName Имя приложения (ПС/ПАСД)
	 * @param browserName Имя браузера
	 * @param proxy прокси
	 * @return
	 */
	public static WebDriver createDriver(URL hubURL, Platform platformName, String appName, String browserName, String proxy, boolean enableLog) {
		DesiredCapabilities cpb = buildCapability(platformName, appName, browserName);

		Proxy oProxy = ProxyUtils.getSeleniumProxy(proxy);
		if (oProxy != null) {
			log.info("Create WebDriver, proxy: " + proxy);
			cpb.setCapability(CapabilityType.PROXY, oProxy);
		}
		else {
			log.info("Create WebDriver, proxy: NONE");
		}

		ChromeOptions options = new ChromeOptions();
		setOptimalChromeOptions(options);
		setDownloadFolder(options, ChromeSettings.DOWNLOAD_FOLDER);
		setLoadExtensions(options, Collections.singletonList(
				ChromeSettings.Extension.NIMBUS));

		if (enableLog){
			LoggingPreferences logPrefs = new LoggingPreferences();
			logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
			options.setCapability("goog:loggingPrefs", logPrefs);
			cpb.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
		}

		cpb.setCapability(ChromeOptions.CAPABILITY, options);
		return new RemoteWebDriver(hubURL, cpb);
	}

	/**
	 * Метод создания selenium драйвера Chrome с кастомным профилем
	 * @param hubURL URL selenium хаба
	 * @param platformName Имя платформы
	 * @param appName Имя приложения (ПС/ПАСД)
	 * @param extensions расширения
	 * @return
	 */
	public static WebDriver createChromeDriver(URL hubURL, Platform platformName, String appName,
											   List<ChromeSettings.Extension> extensions) {
		DesiredCapabilities cpb = buildCapability(
				platformName, appName, "chrome");

        ChromeOptions options = new ChromeOptions();
		setOptimalChromeOptions(options);
		setDownloadFolder(options, ChromeSettings.DOWNLOAD_FOLDER);
		setLoadExtensions(options, extensions);
		cpb.setCapability(ChromeOptions.CAPABILITY, options);
		return new RemoteWebDriver(hubURL, cpb);
    }

    private static void setOptimalChromeOptions(ChromeOptions options){
		options.addArguments("--start-maximized");
		options.addArguments("--ignore-certificate-errors");
		options.addArguments("--disable-popup-blocking");
		//options.addArguments("--headless");
		//options.addArguments("--window-size=1920,1080");
		//options.addArguments("--no-sandbox");				// избавляет от некоторых проблем с таймаутом, рендерингом, но не безопасно!
		options.addArguments("--dns-prefetch-disable");		// отключение предварительной выборки DNS. в теории должно ускорить работу
		options.addArguments("--disable-gpu");				// говорят частично решает проблему с рендерингом (скриншотом)
		options.addArguments("--disable-features=VizDisplayCompositor");	// у кого-то в 73 версии устранило проблему: Timed out receiving message from renderer: 10.000
		//options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable( LogType.PERFORMANCE, Level.ALL );
		logPrefs.enable( LogType.DRIVER, Level.ALL );
		options.setCapability( "goog:loggingPrefs", logPrefs );
	}

	/**
	 * Метод создания параметров драйвера
	 * @param platform Имя платформы
	 * @param appName Имя приложения (ПС/ПАСД)
	 * @param browserName Имя браузера
	 * @return
	 */
	private static DesiredCapabilities buildCapability(Platform platform, String appName, String browserName) {
		DesiredCapabilities capability = createCapabilities(browserName);
		capability.setBrowserName(browserName);
		if(platform != null)
			capability.setPlatform(platform);
		else
			capability.setPlatform(Platform.ANY);
		if(Strings.isNotEmpty(appName)) {
			capability.setCapability("applicationName", appName);
			capability.setCapability("name", appName);
		}
		capability.setCapability("enableVNC", true);
		return capability;
	}

	private static void setLoadExtensions(ChromeOptions options, List<ChromeSettings.Extension> extensions) {
		options.addArguments("--user-data-dir=" + ChromeSettings.USER_DATA_FOLDER);
		options.addArguments("--profile-directory=" + ChromeSettings.PROFILE_NAME);
		options.addArguments("--load-extension=" + ChromeSettings.buildLoadExtensionArgValue(extensions));
		options.addArguments("--auto-select-desktop-capture-source=Entire screen");
	}

	private static void setDownloadFolder(ChromeOptions options, String folder) {
		HashMap<String, Object> chromePrefs = new HashMap<>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", ChromeSettings.DOWNLOAD_FOLDER);
		chromePrefs.put("download.prompt_for_download", "false");

		options.setExperimentalOption("prefs", chromePrefs);
		options.addArguments("--test-type");
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
