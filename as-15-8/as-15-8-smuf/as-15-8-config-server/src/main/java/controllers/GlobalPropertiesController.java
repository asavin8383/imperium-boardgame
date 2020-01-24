package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import exceptions.AS_15_8_Config_Exception;
import lombok.RequiredArgsConstructor;
import model.GlobalProperty;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.GlobalPropertiesRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)

@RequestMapping(path = "/props/global")

public class GlobalPropertiesController {

    private final GlobalPropertiesRepository globalPropertiesRepo;
    @Value("${user.activity.timeout.config.key}")
    private String userActivityTimeoutConfigKey;

    @Value("${kibana.link.key}")
    private String kibanaLinkKey;

    @PreAuthorize("hasRole('ROLE_MANAGE_CONFIGURATIONS')")
    @PostMapping
    @JsonView(Views.Brief.class)
    public List<GlobalProperty> getGlobalProperties() {
        return globalPropertiesRepo.findAll();
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_CONFIGURATIONS')")
    @PutMapping
    public List<GlobalProperty> putGlobalProperties(@RequestBody List<GlobalProperty> newGlobalPropertyList) {
        List<GlobalProperty> newProperites = checkConfiguration(newGlobalPropertyList);
        globalPropertiesRepo.deleteAll();
        return globalPropertiesRepo.saveAll(newProperites);
    }

    private List<GlobalProperty> checkConfiguration(List<GlobalProperty> newGlobalPropertyList) {
        if (newGlobalPropertyList != null) {
            newGlobalPropertyList.removeIf(property -> property.getConfiguration() == null);
            if (newGlobalPropertyList.isEmpty())
                throw new IllegalArgumentException("Список входных параметров пуст");
            return newGlobalPropertyList;
        } else {
            throw new IllegalArgumentException("Список входных параметров null");
        }
    }

    @PostMapping(value = "activity_timeout")
    public Long getActivityTimeOut() {
       String timeout = globalPropertiesRepo.getGlobalPropertyByKey(userActivityTimeoutConfigKey);
        try {
            return Long.valueOf(timeout);
        } catch (Exception e) {
            throw new AS_15_8_Config_Exception("Ошибка извлечения activity_timeout из global_properties " + e);
        }
    }

    @PostMapping(value = "kibana_link")
    public ResponseEntity getKibanaLink() {
        try {
            String kibanaLink = globalPropertiesRepo.getGlobalPropertyByKey(kibanaLinkKey);
            return ResponseEntity.ok().body(kibanaLink);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка извлечения kibana link из global_properties");
        }
    }
}
