package common;

import model.soap.SoapOperation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * User: asinjavin
 * Date: 21.11.2019
 * Time: 22:47
 */
@SpringBootApplication
@ComponentScan(basePackages = {"common", "servlet", "repositories"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
@Configuration
public class SpringBootTomcatApplication extends SpringBootServletInitializer
{
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Map<String, SoapOperation> requests() {
        return new HashMap<>();
    }
}