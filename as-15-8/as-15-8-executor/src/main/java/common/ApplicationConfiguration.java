package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import robots.RobotsConfiguration;

/**
 * Конфигурация модуля поиска в социальных сетях
 * @author shabalinAI
 *
 */
@SpringBootApplication
@Import(RobotsConfiguration.class)
@PropertySource("classpath:application.yml")
@ComponentScan(basePackages={"common", "robots", "service"})
public class ApplicationConfiguration{
	
	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

}
