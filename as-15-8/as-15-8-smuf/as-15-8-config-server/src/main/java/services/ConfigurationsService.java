package services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Configuration;
import model.enums.Microservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repositories.ConfigurationRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConfigurationsService {

    @Value("${spring.cloud.config.server.jdbc.default-label}")
    private String defaultLabel;

    @Value("${spring.cloud.config.server.jdbc.default-profile}")
    private String defaultProfile;

    @Getter
    @Value("#{configurationsService.parseMicroservicesRequiredRobotsConfigs('${microservices.required-robots}')}")
    private List<Configuration> msRequiredRobotsConfigs;

    private final ConfigurationRepository configurationRepository;

    public List<Configuration> parseMicroservicesRequiredRobotsConfigs(String[] microservices) {
        return Stream.of(microservices)
            .map(Microservice::valueOf)
            .map(this::getOrCreate)
            .collect(Collectors.toList());
    }

    private Configuration getOrCreate(Microservice microservice){
        return configurationRepository.findByApplication(microservice)
                .orElseGet(() ->
                        configurationRepository.save(new Configuration(microservice, defaultProfile, defaultLabel)));
    }
}
