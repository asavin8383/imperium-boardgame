package robots;

import enums.AccessToolUnit;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import robots.impl.SeleniumSearchRobot;
import robots.impl.SeleniumVpnRobot;
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
	@Bean
	public Robot googleRobot() throws MalformedURLException {
		return new SeleniumSearchRobot<>(
				AccessToolUnit.GOOGLE,
				new URL(this.seleniumHubUrl),
				GoogleScript.class,
				env.getProperty("robots.google.browser"),
				Platform.valueOf(env.getProperty("robots.google.platform")),
				env.getProperty("robots.google.app"),
				env.getProperty("robots.google.limit"));
	}

	/**
	 * Робот проверки ПС Yandex
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public Robot yandexRobot() throws MalformedURLException {
		return new SeleniumSearchRobot<>(
				AccessToolUnit.YANDEX,
				new URL(this.seleniumHubUrl),
				YandexScript.class,
				env.getProperty("robots.yandex.browser"),
				Platform.valueOf(env.getProperty("robots.yandex.platform")),
				env.getProperty("robots.yandex.app"),
				env.getProperty("robots.yandex.limit"));
	}

	/**
	 * Робот проверки ПАСД (VPN и прокси)
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public Robot vpnRobot() throws MalformedURLException {

		return new SeleniumVpnRobot<>(
				AccessToolUnit.KASPERSKY,
				new URL(this.seleniumHubUrl),
				VPNScript.class,
				env.getProperty("robots.vpn.browser"),
				Platform.valueOf(env.getProperty("robots.vpn.platform")),
				env.getProperty("robots.vpn.app")
		);
	}

	/**
	 * Робот проверки анонимайзера HideMyAss
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public Robot hideMyAssRobot() throws MalformedURLException {

		return new SeleniumVpnRobot<>(
				AccessToolUnit.HIDEMYASS,
				new URL(this.seleniumHubUrl),
				HideMyAssScript.class,
				env.getProperty("robots.hidemyass.browser"),
				Platform.valueOf(env.getProperty("robots.hidemyass.platform")),
				env.getProperty("robots.hidemyass.app")
		);
	}

	/**
	 * Робот проверки анонимайзера CameleoXYZ
	 * @return
	 * @throws MalformedURLException
	 */
	@Bean
	public Robot cameleoXyzRobot() throws MalformedURLException {

		return new SeleniumVpnRobot<>(
				AccessToolUnit.CAMELEO_XYZ,
				new URL(this.seleniumHubUrl),
				CameleoScript.class,
				env.getProperty("robots.cameleoxyz.browser"),
				Platform.valueOf(env.getProperty("robots.cameleoxyz.platform")),
				env.getProperty("robots.cameleoxyz.app")
		);
	}
	
}