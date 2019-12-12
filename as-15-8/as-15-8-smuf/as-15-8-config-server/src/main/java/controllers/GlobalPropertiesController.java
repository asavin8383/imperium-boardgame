package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import model.GlobalProperty;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.GlobalPropertiesRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@PreAuthorize("hasRole('ROLE_MANAGE_CONFIGURATIONS')")
@RequestMapping(path = "/props/global")

public class GlobalPropertiesController {

    private final GlobalPropertiesRepository globalPropertiesRepo;

    @PostMapping
    @JsonView(Views.Brief.class)
    public List<GlobalProperty> getGlobalProperties() {
        return globalPropertiesRepo.findAll();
    }

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
}
