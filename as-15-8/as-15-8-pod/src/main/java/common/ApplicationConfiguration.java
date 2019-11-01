package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.security.Principal;


/**
 * Конфигурация АС 15.8
 *
 */
@SpringBootApplication
@Import(Oauth2ClientConfiguration.class)
@ComponentScan(basePackages= {"common", "repositories", "controllers", "services", "restapi"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
@EnableScheduling
@EnableAsync
@EnableCaching
@RestController
public class ApplicationConfiguration {

	// todo async security
	/*@PostConstruct
	public void init() {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}*/

	@GetMapping("/")
	public String home(Principal user) {
		return "Hello " + user.getName();
	}

    public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

	@PostConstruct
	public void test() {
		System.out.println("START ---------------------");
	}
}
