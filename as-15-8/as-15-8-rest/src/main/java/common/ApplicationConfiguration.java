package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Конфигурация rest-endpoint АС 15.8
 * @author asavin
 *
 */

@SpringBootApplication
@ComponentScan(basePackages= {"common", "services", "repositories", "controllers", "advices", "aspects", "kafka"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
public class ApplicationConfiguration {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

}
