package controllers.traffic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.DynamicTrafficUnit;
import model.traffic.Traffic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.DynamicTrafficUnitRepository;
import services.traffic.TrafficService;

import java.util.List;

@RestController
@RequestMapping(path = "/traffic/unit/dynamic", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class DynamicTrafficController {

    private final TrafficService trafficService;
    private final DynamicTrafficUnitRepository dynamicTrafficUnitRepository;

    @GetMapping
    public ResponseEntity getDynamicTraffic(@RequestParam("trafficId") Traffic traffic) {
        if (traffic != null) {
            DynamicTrafficUnit dynamicTrafficUnit = traffic.getDynamicTrafficUnits().stream().findFirst().orElseThrow(() ->
                    new AS_15_8_PPT_Exception("У трафика id: " + traffic.getId() + " нет динамического трафика"));
            return ResponseEntity.ok(dynamicTrafficUnit);
        } else return ResponseEntity.badRequest().body("Такой динамический трафик не найден в БД");
    }

    @GetMapping(path = "/erdi")
    public Page<ObjectNode> getDynamicErdiIds(@RequestParam(required = false) SortingDirection sortingDirection,
                                              @RequestParam(required = false) String sortingColumn,
                                              @RequestParam(defaultValue = "0") int pageNumber,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              @RequestParam("trafficId") Traffic traffic) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));

        if (traffic != null) {
            List<ObjectNode> erdis = trafficService.getAllErdisForDynamicTraffic(traffic);
            return new PageImpl<>(erdis, pageable, erdis.size());
        } else {
            throw new AS_15_8_PPT_Exception("Not supported");
        }
    }

    @PostMapping()
    public ResponseEntity postDynamicTraffic(@RequestParam("trafficId") Traffic traffic,
                                             @RequestBody DynamicTrafficUnit newDynamicTraffic) {
        if (traffic != null) {
            trafficService.removeAllDynamicTrafficUnits(traffic);
            return ResponseEntity.ok().body(trafficService.addDynamicTrafficUnit(traffic, newDynamicTraffic));
        } else return ResponseEntity.badRequest().body("Такой трафик не обнаружен в БД");
    }

    @DeleteMapping
    public void removeDynamicTrafficUnits(@RequestParam("trafficId") Traffic traffic) {
        if (traffic == null) {
            throw new AS_15_8_PPT_Exception("DynamicTrafficUnit not found");
        } else {
            trafficService.removeAllDynamicTrafficUnits(traffic);
        }
    }

    @PutMapping()
    public ResponseEntity updateDynamicTrafficUnitToTraffic(@RequestParam("trafficId") Traffic traffic,
                                                         @RequestBody DynamicTrafficUnit newDynamicTraffic) {
        if (traffic == null)
            throw new AS_15_8_PPT_Exception("Такой трафик не обнаружен в БД");
        if (newDynamicTraffic != null) {
            DynamicTrafficUnit dynamicTraffic = trafficService.upadateFirstDynamicTrafficUnit(traffic, newDynamicTraffic);
            return ResponseEntity.ok(dynamicTraffic);
        } else return ResponseEntity.badRequest().body("В теле пакета нет динамического трафика");
    }

    //TODO убрать, как только поменяется фронт для трафиков
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addDynamicTrafficQueryToUnit(@PathVariable("id") DynamicTrafficUnit unit, @RequestParam String query) {
        if (unit != null) {
            unit.setQuery(query);
            dynamicTrafficUnitRepository.save(unit);
        }
    }

}
