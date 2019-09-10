package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
public class ApplicationConfiguration {
	
	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

}
