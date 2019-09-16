package controllers;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.CustomErdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiRepository;

@RestController
@RequestMapping(path = "/erdi/custom", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CustomErdiController {

    private final CustomErdiRepository erdiRepository;

    @GetMapping
    public Page<CustomErdi> getAllCustomErdi(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) Long violationId) {
        PageRequest page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        return violationId == null ? erdiRepository.findAll(page) :
                erdiRepository.findByViolationId(page, violationId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public CustomErdi createCustomErdi(@RequestBody CustomErdi customErdi) {
        return erdiRepository.save(customErdi);
    }

    @GetMapping(path = "/{id}")
    public CustomErdi getCustomErdi(@PathVariable Long id) {
        return erdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Custom ERDI was not found by id: " + id));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CustomErdi updateCustomErdi(@RequestBody CustomErdi newCustomErdi,
                                       @PathVariable("id") CustomErdi customErdi) {
        customErdi.setName(newCustomErdi.getName());
        customErdi.setViolation(newCustomErdi.getViolation());
        return erdiRepository.save(customErdi);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCustomErdi(@PathVariable Long id) {
        erdiRepository.deleteById(id);
    }

}
