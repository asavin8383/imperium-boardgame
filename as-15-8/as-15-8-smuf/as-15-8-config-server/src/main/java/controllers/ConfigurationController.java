package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import model.Configuration;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ConfigurationRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@PreAuthorize("hasRole('ROLE_MANAGE_CONFIGURATIONS')")
@RequestMapping(path = "/configurations")

public class ConfigurationController {

    private final ConfigurationRepository configurationRepository;

    @JsonView(Views.Brief.class)
    @PostMapping
    public List<Configuration> getConfigurations(){
        return configurationRepository.findAll();
    }

}
