package services;

import lombok.RequiredArgsConstructor;
import model.Configuration;
import model.Microservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repositories.ConfigurationRepository;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConfigurationsService {

    @Value("${spring.cloud.config.server.jdbc.default-label}")
    private String defaultLabel;

    @Value("${spring.cloud.config.server.jdbc.default-profile}")
    private String defaultProfile;

    private final ConfigurationRepository configurationRepository;

    public Configuration getOrCreate(Microservice microservice){
        return configurationRepository.findByApplication(microservice)
                .orElseGet(() ->
                        configurationRepository.save(new Configuration(microservice, defaultProfile, defaultLabel)));
    }

}
