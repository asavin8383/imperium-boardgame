package robots.factory;

import enums.AccessToolUnit;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import robots.RobotDriverParameters;
import robots.factory.impl.*;
import robots.impl.*;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Конфигурация роботов для проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Configuration
public class RobotsFactoryConfiguration {

	/** Среда выполнения модуля */
	@Autowired
	Environment env;

	/** URL selenium хаба */
	@Value("${selenium-hub-url}")
	private String seleniumHubUrl;

	/**
	 * Робот проверки ПС Google
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory googleRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.GOOGLE,
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.google.platform")),
				env.getProperty("robots.google.app"),
				env.getProperty("robots.google.browser")),
			Integer.parseInt(env.getProperty("robots.google.limit"))
		);
	}
	
	@Bean
	public RobotsFactory googleApiRobot() throws MalformedURLException {
		return new GoogleApiRobotsFactory(
			AccessToolUnit.GOOGLE_API,
			GoogleApiRobot.class,
			env.getProperty("robots.google-api.search-system-id"),
			env.getProperty("robots.google-api.key"),
			env.getProperty("robots.google-api.region"),
			Integer.parseInt(env.getProperty("robots.google-api.search-limit"))
		);
	}

	/**
	 * Робот проверки ПС Yandex
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory yandexRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.YANDEX, // YandexRobot
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.yandex.platform")),
				env.getProperty("robots.yandex.app"),
				env.getProperty("robots.yandex.browser")),
			Integer.parseInt(env.getProperty("robots.yandex.limit"))
		);
	}

	/**
	 * Робот проверки ПС Mail.ru
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory mailRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.MAIL,
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.search-system-direct.platform")),
				env.getProperty("robots.search-system-direct.app"),
				env.getProperty("robots.search-system-direct.browser")),
			Integer.parseInt(env.getProperty("robots.search-system-direct.limit"))
		);
	}

	/**
	 * Робот проверки ПС Yahoo
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory yahooRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.YAHOO,
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
					Platform.valueOf(env.getProperty("robots.search-system-direct.platform")),
					env.getProperty("robots.search-system-direct.app"),
					env.getProperty("robots.search-system-direct.browser")),
			Integer.parseInt(env.getProperty("robots.search-system-direct.limit"))
		);
	}

	/**
	 * Робот проверки ПС Bing
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory bingRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.BING,
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
					Platform.valueOf(env.getProperty("robots.search-system-direct.platform")),
					env.getProperty("robots.search-system-direct.app"),
					env.getProperty("robots.search-system-direct.browser")),
			Integer.parseInt(env.getProperty("robots.search-system-direct.limit"))
		);
	}

	/**
	 * Робот проверки ПС Sputnik
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory sputnikRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.SPUTNIK,
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
					Platform.valueOf(env.getProperty("robots.search-system-direct.platform")),
					env.getProperty("robots.search-system-direct.app"),
					env.getProperty("robots.search-system-direct.browser")),
			Integer.parseInt(env.getProperty("robots.search-system-direct.limit"))
		);
	}

	/**
	 * Робот проверки ПС DuckDuckGo
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory duckDuckGoRobot() throws MalformedURLException {
		return new SeleniumSearchRobotsFactory(
			AccessToolUnit.DUCK_DUCK_GO,
			CommonDirectSearchRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
					Platform.valueOf(env.getProperty("robots.search-system-direct.platform")),
					env.getProperty("robots.search-system-direct.app"),
					env.getProperty("robots.search-system-direct.browser")),
			Integer.parseInt(env.getProperty("robots.search-system-direct.limit"))
		);
	}

	/**
	 * Робот проверки ПАСД (VPN)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory kasperskyRobot() throws MalformedURLException {

		return new SeleniumVpnRobotsFactory(
			AccessToolUnit.KASPERSKY,
			VPNRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.kaspersky.platform")),
				env.getProperty("robots.kaspersky.app"),
				env.getProperty("robots.kaspersky.browser")
			)
		);
	}
	
	/**
	 * Робот проверки ПАСД (VPN-Express)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory expressRobot() throws MalformedURLException {

		return new SeleniumVpnRobotsFactory(
			AccessToolUnit.EXPRESS,
			VPNRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.express.platform")),
				env.getProperty("robots.express.app"),
				env.getProperty("robots.express.browser")
			)
		);
	}

	/**
	 * Робот проверки ПАСД (VPN)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory holaRobot() throws MalformedURLException {

		return new SeleniumHolaRobotsFactory(
			AccessToolUnit.HOLA,
			HolaRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.hola.platform")),
				env.getProperty("robots.hola.app"),
				env.getProperty("robots.hola.browser")
			),
			env.getProperty("robots.hola.crx-file-path")
		);
	}

	/**
	 * Робот проверки ПАСД (Прокси)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory proxyTorGuardRobot() throws MalformedURLException {

		return new SeleniumVpnRobotsFactory(
			AccessToolUnit.TORGUARD,
			VPNRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.torguard.platform")),
				env.getProperty("robots.torguard.app"),
				env.getProperty("robots.torguard.browser")
			)
		);
	}

	/**
	 * Робот проверки анонимайзера HideMyAss
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory hideMyAssRobot() throws MalformedURLException {

		return new SeleniumAnonymizerRobotsFactory(
			AccessToolUnit.HIDEMYASS,
			HideMyAssRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.hidemyass.platform")),
				env.getProperty("robots.hidemyass.app"),
				env.getProperty("robots.hidemyass.browser")
			)
		);
	}

	/**
	 * Робот проверки анонимайзера CameleoXYZ
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public RobotsFactory cameleoXyzRobot() throws MalformedURLException {

		return new SeleniumAnonymizerRobotsFactory(
			AccessToolUnit.CAMELEO_XYZ,
			CameleoRobot.class,
			new RobotDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.cameleoxyz.platform")),
				env.getProperty("robots.cameleoxyz.app"),
				env.getProperty("robots.cameleoxyz.browser")
			)
		);
	}
}