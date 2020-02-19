package controllers.traffic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.DynamicTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.DynamicTrafficUnitRepository;

@RestController
@RequestMapping(path = "/traffic/unit/dynamic", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class DynamicTrafficController {

    private final DynamicTrafficUnitRepository dynamicTrafficUnitRepository;

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addDynamicTrafficQueryToUnit(@PathVariable("id") DynamicTrafficUnit unit, @RequestParam String query) {
        if (unit != null) {
            unit.setQuery(query.replace("&", "%26"));
            dynamicTrafficUnitRepository.save(unit);
        }
    }
}
