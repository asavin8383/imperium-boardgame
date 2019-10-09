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
                                             @RequestParam(defaultValue = "false") boolean returnAll,
                                             @RequestParam(required = false) Long erdiTrafficUnitId,
                                             @RequestParam(required = false) Long searchTrafficUnitId,
                                             @RequestParam(required = false) String query,
                                             @RequestParam(required = false) Long resourceTypeId) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        FormalErdiParams params = new FormalErdiParams(returnAll,
                erdiTrafficUnitId, searchTrafficUnitId, query, resourceTypeId);
        Page<FormalErdi> result =
                formalErdiRepository.searchFor(FormalErdi.class, params, page);

        if (returnAll) {
            if (erdiTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(formalErdiRepository
                        .belongsToErdiTrafficUnit(erdiTrafficUnitId, rec.getId())));
            else if (searchTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(formalErdiRepository
                        .belongsToSearchTrafficUnit(searchTrafficUnitId, rec.getId())));
        }
        return result;
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public FormalErdi getFormalErdiById(@PathVariable Long id) {
        return formalErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Formal ERDI was not found by id: " + id));
    }

}
