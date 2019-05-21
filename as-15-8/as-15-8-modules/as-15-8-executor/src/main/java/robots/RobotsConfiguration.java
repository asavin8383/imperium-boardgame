package robots;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import enums.AccessToolUnit;
import robots.impl.SeleniumRobot;
import scripts.impl.GoogleScript;

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
		return new SeleniumRobot<GoogleScript>(
			AccessToolUnit.GOOGLE,
			new URL(this.seleniumHubUrl),
			GoogleScript.class,
			env.getProperty("robots.google.browser"),
			Platform.valueOf(env.getProperty("robots.google.platform")),
			env.getProperty("robots.google.app"));
	}
	
}