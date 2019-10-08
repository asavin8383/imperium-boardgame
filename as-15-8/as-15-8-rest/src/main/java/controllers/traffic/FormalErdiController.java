package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.erdi.FormalErdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.FormalErdiRepository;
import repositories.helpers.FormalErdiParams;

@RestController
@RequestMapping(path = "/erdi/formal", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalErdiController {

    private final FormalErdiRepository formalErdiRepository;

    @GetMapping
    public Page<FormalErdi> getAllFormalErdi(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "true") boolean belongsTo,
                                             @RequestParam(required = false) Long trafficUnitId,
                                             @RequestParam(required = false) String query,
                                             @RequestParam(required = false) Long resourceTypeId) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        FormalErdiParams params = new FormalErdiParams(
                belongsTo, trafficUnitId, query, resourceTypeId);
        return formalErdiRepository.searchFor(FormalErdi.class, params, page);
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public FormalErdi getFormalErdiById(@PathVariable Long id) {
        return formalErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Formal ERDI was not found by id: " + id));
    }

}
