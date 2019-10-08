package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.CustomErdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiRepository;
import repositories.helpers.CustomErdiParams;

@RestController
@RequestMapping(path = "/erdi/custom", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CustomErdiController {

    private final CustomErdiRepository customErdiRepository;

    @GetMapping
    public Page<CustomErdi> getAllCustomErdi(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "true") boolean belongsTo,
                                             @RequestParam(required = false) Long trafficUnitId,
                                             @RequestParam(required = false) String query,
                                             //@RequestParam(required = false) Long resourceTypeId,
                                             @RequestParam(required = false) Long violationId) {
        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        CustomErdiParams params = new CustomErdiParams(
                belongsTo, trafficUnitId, query, violationId);
        return customErdiRepository.searchFor(CustomErdi.class, params, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    @JsonView(Views.Full.class)
    public CustomErdi createCustomErdi(@RequestBody CustomErdi customErdi) {
        return customErdiRepository.save(customErdi);
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public CustomErdi getCustomErdiById(@PathVariable Long id) {
        return customErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Custom ERDI was not found by id: " + id));
    }

    @Transactional
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public CustomErdi updateCustomErdi(@RequestBody CustomErdi newCustomErdi,
                                       @PathVariable("id") CustomErdi customErdi) {
        customErdi.setName(newCustomErdi.getName());
        customErdi.setViolation(newCustomErdi.getViolation());
        //customErdi.setCustomErdiUnits(newCustomErdi.getCustomErdiUnits());
        return customErdiRepository.save(customErdi);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCustomErdi(@PathVariable Long id) {
        customErdiRepository.deleteById(id);
    }

}
