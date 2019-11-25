package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.CustomErdi;
import model.traffic.CustomErdiView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiViewRepository;
import services.traffic.CustomErdiService;

@RestController
@RequestMapping(path = "/erdi/custom", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CustomErdiController {

    private final CustomErdiService customErdiService;
    private final CustomErdiViewRepository customErdiViewRepository;

    @GetMapping
    public Page<CustomErdiView> getCustomErdiRows(@RequestParam(required = false) SortingDirection sortingDirection,
                                                  @RequestParam(required = false) String sortingColumn,
                                                  @RequestParam(defaultValue = "0") int pageNumber,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) String query,
                                                  @RequestParam(required = false) Long erdiTrafficUnitId,
                                                  @RequestParam(required = false) Long searchTrafficUnitId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        if(query == null) {
            query = "";
        }
        if(erdiTrafficUnitId != null) {
            return customErdiViewRepository.findAllByErdiTrafficUnitsContainingAndQuery(erdiTrafficUnitId, query, pageable);
        } else if (searchTrafficUnitId != null) {
            return customErdiViewRepository.findAllBySearchQueryTrafficUnitsContainingAndQuery(searchTrafficUnitId, query, pageable);
        } else {
            return customErdiViewRepository.findAllByQuery(query, pageable);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    @JsonView(Views.Full.class)
    public CustomErdi createCustomErdi(@RequestBody CustomErdi customErdi) {
        return customErdiService.createCustomErdi(customErdi);
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public CustomErdi getCustomErdiById(@PathVariable Long id) {
        return customErdiService.getCustomErdiById(id);
    }

    @Transactional
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public CustomErdi updateCustomErdi(@RequestBody CustomErdi newCustomErdi,
                                       @PathVariable("id") CustomErdi customErdi) {
        return customErdiService.updateCustomErdi(newCustomErdi, customErdi);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCustomErdi(@PathVariable Long id) {
        customErdiService.deleteCustomErdi(id);
    }

}
