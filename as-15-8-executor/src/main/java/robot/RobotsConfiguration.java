package robot;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import enums.AccessTool;
import robot.impl.SeleniumRobot;
import scripts.impl.GoogleScript;

@Configuration
public class RobotsConfiguration {

	/** Среда выполнения модуля */
	@Autowired
	Environment env;
	
	@Value("${selenium-hub-url}")
	private String seleniumHubUrl;
	
	@Bean
	public Robot googleRobot() throws MalformedURLException {
		return new SeleniumRobot(
			AccessTool.GOOGLE,
			GoogleScript.class,
			new URL(seleniumHubUrl),
			Platform.valueOf(env.getProperty("robots.google.platform")),
			env.getProperty("robots.google.app"),
			env.getProperty("robots.google.browser"));
	}
	
}
