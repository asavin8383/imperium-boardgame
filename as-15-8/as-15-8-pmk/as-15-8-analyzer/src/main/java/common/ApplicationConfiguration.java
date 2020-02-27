package common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import utils.ScreenshotAnalyzerHelper;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"common", "events", "service", "restapi"})
@EnableConfigurationProperties(AnalyzerProperties.class)
public class ApplicationConfiguration{

	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

	@Bean
	public ScreenshotAnalyzerHelper screenshotAnalyzerHelper(){
		return new ScreenshotAnalyzerHelper();
	}
}
