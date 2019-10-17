package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"common", "events", "robots.factory", "service"})
@EnableConfigurationProperties(ExecutorProperties.class)
public class ApplicationConfiguration{
	
	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}
}
