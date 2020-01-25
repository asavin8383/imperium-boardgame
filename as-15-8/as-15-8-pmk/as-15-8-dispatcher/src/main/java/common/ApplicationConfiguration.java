package common;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import model.serializers.PageSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import remoteEvents.ArrangementStopEvent;

/**
 * Конфигурация модуля запуска проверок мероприятий
 * @author shabalinAI
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages={"common", "controllers", "services", "repositories", "events", "webClients", "restapi"})
@RemoteApplicationEventScan(basePackageClasses = {ArrangementStopEvent.class})
@EntityScan("model")
@EnableJpaRepositories("repositories")
public class ApplicationConfiguration{

	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

	@Bean
	public Module jacksonPageWithJsonViewModule() {
		SimpleModule module = new SimpleModule("jackson-page-with-jsonview", Version.unknownVersion());
		module.addSerializer(PageImpl.class, new PageSerializer());
		return module;
	}
}
