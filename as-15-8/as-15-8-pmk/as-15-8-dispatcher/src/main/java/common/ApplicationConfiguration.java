package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"common", "controllers", "services", "repositories", "events", "webClients", "restapi"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
@EnableScheduling
public class ApplicationConfiguration{

	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

}
