package common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Arrays;


/**
 * Конфигурация АС 15.8
 *
 */
@SpringBootApplication
@Import(RestTemplateConfiguration.class)
@ComponentScan(basePackages= {"common", "repositories", "controllers", "services", "restapi"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
@EnableScheduling
@EnableAsync
@EnableResourceServer
@RestController
public class ApplicationConfiguration extends ResourceServerConfigurerAdapter {

	@Value("${jwt.key}")
	private String jwtKey;

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

	@Override
	public void configure(final HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/oauth/**")
				.permitAll()
				.antMatchers("/**")
				.authenticated();
	}

	@PostConstruct
	public void test() {
		System.out.println("START ---------------------");
	}
}
