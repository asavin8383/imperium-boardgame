package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages= {"common", "services", "repositories", "controllers", "events"})
@EnableConfigurationProperties(SchedulerProperties.class)
@EnableJpaRepositories("repositories")
@EntityScan("model")
public class SchedulerApplicationConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplicationConfiguration.class, args);
    }

}