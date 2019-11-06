package controllers.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ErdiContentJoinRepository;
import repositories.ErdiTrafficUnitRepository;

@RestController
@RequestMapping(path = "/erdi/formal", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalErdiController {

    private final ErdiTrafficUnitRepository erdiTrafficUnitRepository;
    private final ErdiContentJoinRepository erdiContentJoinRepository;

    @GetMapping
    public Page<Long> getFormalErdiIds(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "false") boolean containsInTraffic,
                                             @RequestParam(required = false) Long erdiTrafficUnitId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        if (erdiTrafficUnitId != null) {
            return containsInTraffic ?
                    erdiContentJoinRepository.findContentIdByTrafficUnit(erdiTrafficUnitId, pageable) :
                    erdiContentJoinRepository.findContentIdNotInTrafficUnit(erdiTrafficUnitId, pageable);
        } else {
            throw new AS_15_8_PPT_Exception("Not supported");
        }
    }

}
