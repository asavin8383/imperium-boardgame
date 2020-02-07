package common;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import model.serializers.PageSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAsync
@EnableConfigServer
@ComponentScan(basePackages = {"common", "model", "controllers", "services", "repositories", "handlers"})
@EntityScan("model")
@EnableJpaRepositories("repositories")
@EnableDiscoveryClient
@RestController
public class SpringConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringConfigServerApplication.class, args);
    }

    @Bean
    public Module jacksonPageWithJsonViewModule() {
        SimpleModule module = new SimpleModule("jackson-page-with-jsonview", Version.unknownVersion());
        module.addSerializer(PageImpl.class, new PageSerializer());
        return module;
    }

}
