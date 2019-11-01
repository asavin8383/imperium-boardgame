package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.Traffic;
import model.traffic.projection.TrafficProjection;
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
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping
    public Page<TrafficProjection> getAllTrafficInfo(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String query) {

        return trafficService.getAllTrafficInfo(
                sortingDirection, sortingColumn, pageNumber, pageSize, query);
    }

    @Transactional
    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public Traffic getTrafficById(@PathVariable Long id) {
        return trafficService.getTrafficById(id);
    }

    @Transactional
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    @JsonView(Views.Full.class)
    public Traffic createTraffic(@RequestBody Traffic traffic) {
        return trafficService.createTraffic(traffic);
    }

    @Transactional
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public Traffic updateTraffic(@RequestBody Traffic newTraffic,
                                 @PathVariable("id") Traffic oldTraffic) {
        return trafficService.updateTraffic(newTraffic, oldTraffic);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteTraffic(@PathVariable Long id) {
        trafficService.deleteTraffic(id);
    }

}
