package controllers;

import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.CustomErdiUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiUnitRepository;

import java.util.List;

@RestController
@RequestMapping(path = "/erdi/custom/units",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CustomErdiUnitController {

    private final CustomErdiUnitRepository unitRepository;

    @GetMapping
    public List<CustomErdiUnit> getAllCustomErdiUnit(@RequestParam Long customId) {
        return unitRepository.findByCustomErdiIdOrderById(customId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)  // todo check customErdiId is sufficient
    public CustomErdiUnit createCustomErdiUnit(@RequestBody CustomErdiUnit customErdiUnit) {
        return unitRepository.save(customErdiUnit);
    }

    @GetMapping(path = "/{id}")
    public CustomErdiUnit getCustomErdiUnit(@PathVariable Long id) {
        return unitRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Custom ERDI unit was not found by id: " + id));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CustomErdiUnit updateCustomErdiUnit(@RequestBody CustomErdiUnit newUnit,
                                               @PathVariable("id") CustomErdiUnit unit) {
        return unitRepository.save(merge(newUnit, unit));
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCustomErdiUnit(@PathVariable Long id) {
        unitRepository.deleteById(id);
    }

    private CustomErdiUnit merge(CustomErdiUnit newUnit, CustomErdiUnit unit) {
        unit.setValue(newUnit.getValue());
        unit.setType(newUnit.getType());
        unit.setCustomErdi(newUnit.getCustomErdi());
        return unit;
    }

}
