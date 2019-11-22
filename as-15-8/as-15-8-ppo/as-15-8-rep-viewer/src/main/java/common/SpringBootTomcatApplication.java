package common;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * User: asinjavin
 * Date: 21.11.2019
 * Time: 22:47
 */
@SpringBootApplication
@ComponentScan(basePackages = {"common", "servlet", "repositories"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
public class SpringBootTomcatApplication extends SpringBootServletInitializer
{
}