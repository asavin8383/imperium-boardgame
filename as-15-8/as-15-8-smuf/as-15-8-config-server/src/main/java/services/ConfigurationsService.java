package services;

import lombok.RequiredArgsConstructor;
import model.Configuration;
import model.Microservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ConfigurationRepository;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConfigurationsService {

    private final ConfigurationRepository configurationRepository;

    public Configuration getOrCreate(Microservice microservice){
        return configurationRepository.findByApplication(microservice)
                .orElseGet(() ->
                        configurationRepository.save(new Configuration(microservice)));
    }

}
