package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import kafka.KafkaConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
@Import({KafkaConfiguration.class})
@PropertySource("classpath:application.yml")
@ComponentScan(basePackages={"common", "kafka", "services", "repositories"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
public class ApplicationConfiguration{
	
	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

}
