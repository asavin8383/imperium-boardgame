package controllers.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.CustomErdi;
import model.traffic.CustomErdiUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiUnitRepository;

@RestController
@RequestMapping(path = "/erdi/custom/units",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CustomErdiUnitController {

    private final CustomErdiUnitRepository unitRepository;

    @GetMapping
    public Page<CustomErdiUnit> getAllCustomErdiUnit(@RequestParam(required = false) SortingDirection sortingDirection,
                                                     @RequestParam(required = false) String sortingColumn,
                                                     @RequestParam(defaultValue = "0") int pageNumber,
                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                     @RequestParam Long customErdiId) {
        PageRequest page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        return unitRepository.findByCustomErdiId(customErdiId, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public CustomErdiUnit createCustomErdiUnit(@RequestBody CustomErdiUnit customErdiUnit,
                                               @RequestParam("customErdiId")CustomErdi customErdi) {
        customErdiUnit.setCustomErdi(customErdi);
        return unitRepository.save(customErdiUnit);
    }

    @GetMapping(path = "/{id}")
    public CustomErdiUnit getCustomErdiUnit(@PathVariable Long id) {
        return unitRepository.findById(id).orElseThrow(() ->
                new AS_15_8_PPT_Exception("Custom ERDI unit was not found by id: " + id));
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
        if (newUnit.getCustomErdi() != null)
            unit.setCustomErdi(newUnit.getCustomErdi());
        return unit;
    }

}
