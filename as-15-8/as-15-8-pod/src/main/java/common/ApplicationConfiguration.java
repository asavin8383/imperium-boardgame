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
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.security.Principal;


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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApplicationConfiguration extends ResourceServerConfigurerAdapter {

	private final CustomAccessTokenConverter customAccessTokenConverter;

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
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.and()
				.authorizeRequests().anyRequest().permitAll();
	}

	@Primary
	@Bean
	public RemoteTokenServices tokenServices() {
		final RemoteTokenServices tokenService = new RemoteTokenServices();
		tokenService.setCheckTokenEndpointUrl("http://localhost:15880/oauth/check_token");
		tokenService.setClientId("as-15-8");
		tokenService.setClientSecret("as-15-8");
		return tokenService;

	}

	/*@Override
	public void configure(final ResourceServerSecurityConfigurer config) {
		config.tokenServices(tokenServices());
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setAccessTokenConverter(customAccessTokenConverter);
		converter.setSigningKey(jwtKey);
		converter.setVerifier(new RsaVerifier(jwtKey));
		//converter.setVerifierKey(jwtKey);
		// final Resource resource = new ClassPathResource("public.txt");
		// String publicKey = null;
		// try {
		// publicKey = IOUtils.toString(resource.getInputStream());
		// } catch (final IOException e) {
		// throw new RuntimeException(e);
		// }
		// converter.setVerifierKey(publicKey);
		return converter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		return defaultTokenServices;
	}*/

	@PostConstruct
	public void test() {
		System.out.println("START ---------------------");
	}
}
