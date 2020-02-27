package controllers.traffic;


import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.ErdiTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.traffic.TrafficService;

@RestController
@RequestMapping(path = "/erdi/formal", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_PREPARATION_TRAFFIC')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalErdiController {

    private final TrafficService trafficService;

    @GetMapping
    public ResponseEntity<Page<ObjectNode>> getFormalErdiIds(@RequestParam(required = false) SortingDirection sortingDirection,
                                                             @RequestParam(required = false) String sortingColumn,
                                                             @RequestParam(defaultValue = "0") int pageNumber,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam("erdiTrafficUnitId") ErdiTrafficUnit erdiTrafficUnit
    ) {
        if (erdiTrafficUnit != null) {

            Pageable pageable = PageRequest.of(pageNumber, pageSize,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));

            if (pageable.getSort().isSorted()) {
                return trafficService.getSortedContentViewFromPod(erdiTrafficUnit, pageable);
            } else {
                return ResponseEntity.ok().body(trafficService.getUnsortedContentViewFromPod(erdiTrafficUnit, pageable));
            }

        } else {
            throw new AS_15_8_PPT_Exception("Not supported");
        }
    }

}
