package robots;

import java.net.MalformedURLException;

import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import enums.AccessTool;
import scripts.GoogleScript;

@Configuration
public class RobotsConfiguration {

	/** Среда выполнения модуля */
	@Autowired
	Environment env;
	
	@Bean
	public Robot<?> googleRobot() throws MalformedURLException {
		return new Robot<GoogleScript>(
			AccessTool.GOOGLE,
			GoogleScript.class,
			env.getProperty("robots.google.browser"),
			Platform.valueOf(env.getProperty("robots.google.platform")),
			env.getProperty("robots.google.app"));
	}
	
}