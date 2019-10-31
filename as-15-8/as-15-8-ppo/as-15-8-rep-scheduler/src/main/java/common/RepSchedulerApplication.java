package common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Стартовый класс
 *
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 16:02
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"model", "controllers", "schedule"})
@EntityScan("model")
@EnableJpaRepositories("repositories")
public class RepSchedulerApplication
{
    public static void main(String[] args) {
        SpringApplication.run(RepSchedulerApplication.class, args);
    }


}