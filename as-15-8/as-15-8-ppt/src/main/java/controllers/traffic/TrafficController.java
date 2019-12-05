package controllers.traffic;

import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.AccessToolType;
import model.traffic.Traffic;
import model.traffic.TrafficBriefView;
import model.traffic.TrafficFullView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import services.traffic.TrafficService;

@RestController
@RequestMapping(path = "/traffic",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_PREPARATION_TRAFFIC')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping
    public Page<TrafficBriefView> getBriefTrafficList(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) AccessToolType accessToolType) {

        return trafficService.getBriefTrafficList(sortingDirection, sortingColumn,
                pageNumber, pageSize, query, accessToolType);
    }

    @Transactional
    @GetMapping(path = "/{id}")
    public TrafficFullView getTrafficById(@PathVariable Long id) {
        return trafficService.getTrafficById(id);
    }

    @Transactional
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public TrafficFullView createTraffic() {
        return trafficService.createTraffic();
    }

    @Transactional
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TrafficFullView updateTraffic(@RequestBody TrafficFullView fullView,
                                         @PathVariable("id") Traffic traffic) {
        return trafficService.updateTraffic(fullView, traffic);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteTraffic(@PathVariable Long id) {
        trafficService.deleteTraffic(id);
    }

}
