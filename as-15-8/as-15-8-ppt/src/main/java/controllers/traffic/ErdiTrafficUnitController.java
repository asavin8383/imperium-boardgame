package controllers.traffic;

import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.TrafficUnitType;
import model.traffic.ErdiTrafficUnit;
import model.traffic.ErdiTrafficUnitContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import repositories.CustomErdiRepository;
import repositories.ErdiContentJoinRepository;
import repositories.ErdiTrafficUnitRepository;
import services.traffic.TrafficService;
import webClients.PodWebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/traffic/unit/erdi", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ErdiTrafficUnitController {

    private final ErdiTrafficUnitRepository erdiTrafficUnitRepository;
    private final CustomErdiRepository customErdiRepository;
    private final ErdiContentJoinRepository erdiContentJoinRepository;
    private final PodWebClient podWebClient;
    private final TrafficService trafficService;

    @PutMapping(path = "/{id}/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addErdiToUnit(@PathVariable("id") ErdiTrafficUnit unit, @RequestBody List<Long> ids) {
        saveErdi(unit, ids);
    }

    @PutMapping(path = "/{id}/addFromPod")
    public List<Long> addErdiToUnitFromPod(
            @PathVariable("id") ErdiTrafficUnit unit,
            @RequestParam(required = false) String idMask,
            @RequestParam(required = false) List<String> categoryNames,
            @RequestParam(required = false) List<String> decisionOrgs,
            @RequestParam(required = false) List<String> infoTypeIds,
            @RequestParam(required = false) List<String> registryNames,
            @RequestParam(required = false) List<String> resourceTypes,
            @RequestParam(required = false) String resourceValue,
            @RequestParam(required = false) List<String> violationNames,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime,
            @RequestParam(required = false) Boolean random,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(required = false) Long visitorsCntRussiaMin,
            @RequestParam(required = false) Long visitorsCntRussiaMax,
            @RequestParam(required = false) Long visitorsCntWorldMin,
            @RequestParam(required = false) Long visitorsCntWorldMax
            ) {

        Flux<List<Long>> idss = podWebClient.getErdiIdList(idMask, categoryNames, decisionOrgs, infoTypeIds,
                registryNames, resourceTypes, resourceValue, violationNames, size,
                startTime, endTime, random, sortingDirection, sortingColumn, visitorsCntRussiaMin, visitorsCntRussiaMax,
                visitorsCntWorldMin, visitorsCntWorldMax);

        List<Long> ids = idss.flatMap(Flux::fromIterable).collectList().block();
        if (ids != null) {
            saveErdi(unit, ids);
        }
        trafficService.actualizeTrafficCheckUnitsCount(unit.getTraffic().getId());
        return ids;
    }

    private void saveErdi(ErdiTrafficUnit unit, List<Long> ids) {
        if (unit == null)
            throw new AS_15_8_PPT_Exception("ErdiTrafficUnit not found");

        if (unit.getType() == TrafficUnitType.FORMAL) {
            List<ErdiTrafficUnitContent> records = ids.stream()
                    .map(id -> new ErdiTrafficUnitContent(unit, id))
                    .collect(Collectors.toList());
            unit.getFormalErdiList().addAll(records);
        } else {
            // assert unit.getType() == TrafficUnitType.CUSTOM
            unit.getCustomErdiList().addAll(customErdiRepository.findAllById(ids));
        }
        erdiTrafficUnitRepository.save(unit);
        trafficService.actualizeTrafficCheckUnitsCount(unit.getTraffic().getId());
    }

    @PutMapping(path = "/{id}/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeErdiFromUnit(@PathVariable("id") ErdiTrafficUnit unit, @RequestBody List<Long> ids) {
        if (unit == null)
            throw new AS_15_8_PPT_Exception("ErdiTrafficUnit not found");

        if (unit.getType() == TrafficUnitType.FORMAL) {
            unit.getFormalErdiList().removeAll(
                    erdiContentJoinRepository.findAllByTrafficUnitAndErdiIdIn(unit, ids));
        } else {
            // assert unit.getType() == TrafficUnitType.CUSTOM
             unit.getCustomErdiList().removeAll(customErdiRepository.findAllById(ids));
        }
        unit.getTraffic().setActualCheckUnitsCount(0L);
        erdiTrafficUnitRepository.save(unit);
    }

}
