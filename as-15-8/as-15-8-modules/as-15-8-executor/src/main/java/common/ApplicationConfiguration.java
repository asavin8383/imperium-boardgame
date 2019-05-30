package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import kafka.KafkaConfiguration;
import robots.RobotsConfiguration;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
@Import({RobotsConfiguration.class, KafkaConfiguration.class})
@PropertySource("classpath:application.yml")
@ComponentScan(basePackages={"common", "kafka", "robots", "service", "listener"})
public class ApplicationConfiguration{
	
	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

}
