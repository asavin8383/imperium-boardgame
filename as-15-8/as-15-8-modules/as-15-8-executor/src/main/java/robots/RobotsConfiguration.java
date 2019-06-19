package robots;

import enums.AccessToolUnit;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import robots.impl.GoogleApiRobot;
import robots.impl.SeleniumAnonymizerRobot;
import robots.impl.SeleniumHolaRobot;
import robots.impl.SeleniumSearchRobot;
import robots.impl.SeleniumVpnRobot;
import scripts.ScriptDriverParameters;
import scripts.impl.*;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Конфигурация роботов для проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Configuration
public class RobotsConfiguration {

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
	/*@Bean
	public Robot googleRobot() throws MalformedURLException {
		return new SeleniumSearchRobot(
			AccessToolUnit.GOOGLE,
			GoogleScript.class,
			new ScriptDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.google.platform")),
				env.getProperty("robots.google.app"),
				env.getProperty("robots.google.browser")),
			Integer.parseInt(env.getProperty("robots.google.limit"))
		);
	}*/
	public Robot googleRobot() throws MalformedURLException {
		return new GoogleApiRobot(
			AccessToolUnit.GOOGLE,
			GoogleApiScript.class,
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
	public Robot yandexRobot() throws MalformedURLException {
		return new SeleniumSearchRobot(
			AccessToolUnit.YANDEX,
			YandexScript.class,
			new ScriptDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.yandex.platform")),
				env.getProperty("robots.yandex.app"),
				env.getProperty("robots.yandex.browser")),
			Integer.parseInt(env.getProperty("robots.yandex.limit"))
		);
	}

	/**
	 * Робот проверки ПАСД (VPN)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public Robot kasperskyRobot() throws MalformedURLException {

		return new SeleniumVpnRobot(
			AccessToolUnit.KASPERSKY,
			VPNScript.class,
			new ScriptDriverParameters(
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
	public Robot expressRobot() throws MalformedURLException {

		return new SeleniumVpnRobot(
			AccessToolUnit.EXPRESS,
			VPNScript.class,
			new ScriptDriverParameters(
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
	public Robot holaRobot() throws MalformedURLException {

		return new SeleniumHolaRobot(
			AccessToolUnit.HOLA,
			HolaScript.class,
			new ScriptDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.hola.platform")),
				env.getProperty("robots.hola.app"),
				env.getProperty("robots.hola.browser")
			)
		);
	}

	/**
	 * Робот проверки ПАСД (Прокси)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public Robot proxyTorGuardRobot() throws MalformedURLException {

		return new SeleniumVpnRobot(
			AccessToolUnit.TORGUARD,
			VPNScript.class,
			new ScriptDriverParameters(
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
	public Robot hideMyAssRobot() throws MalformedURLException {

		return new SeleniumAnonymizerRobot(
			AccessToolUnit.HIDEMYASS,
			HideMyAssScript.class,
			new ScriptDriverParameters(
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
	public Robot cameleoXyzRobot() throws MalformedURLException {

		return new SeleniumAnonymizerRobot(
			AccessToolUnit.CAMELEO_XYZ,
			CameleoScript.class,
			new ScriptDriverParameters(
				new URL(this.seleniumHubUrl),
				Platform.valueOf(env.getProperty("robots.cameleoxyz.platform")),
				env.getProperty("robots.cameleoxyz.app"),
				env.getProperty("robots.cameleoxyz.browser")
			)
		);
	}

}