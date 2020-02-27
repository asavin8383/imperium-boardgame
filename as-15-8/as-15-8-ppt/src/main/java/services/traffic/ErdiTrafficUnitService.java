package services.traffic;

import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import model.enums.TrafficUnitType;
import model.traffic.ErdiTrafficUnit;
import model.traffic.ErdiTrafficUnitContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import repositories.CustomErdiRepository;
import repositories.DynamicTrafficUnitRepository;
import repositories.ErdiTrafficUnitRepository;
import webClients.PodWebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErdiTrafficUnitService {

    private final PodWebClient podWebClient;
    private final TrafficService trafficService;
    private final ErdiTrafficUnitRepository erdiTrafficUnitRepository;
    private final CustomErdiRepository customErdiRepository;

    private final DynamicTrafficUnitRepository dynamicTrafficUnitRepository;

    public List<Long> saveErdis(ErdiTrafficUnit unit,
                          String idMask,
                          List<String> categoryNames,
                          List<String> decisionOrgs,
                          List<String> infoTypeIds,
                          List<String> registryNames,
                          List<String> resourceTypes,
                          String resourceValue,
                          List<String> violationNames,
                          Integer size,
                          LocalDate startTime,
                          LocalDate endTime,
                          Boolean random,
                          SortingDirection sortingDirection,
                          String sortingColumn,
                          Long visitorsCntRussiaMin,
                          Long visitorsCntRussiaMax,
                          Long visitorsCntWorldMin,
                          Long visitorsCntWorldMax
    ) {
        Flux<List<Long>> idss = podWebClient.getErdiIdList(idMask, categoryNames, decisionOrgs, infoTypeIds,
                registryNames, resourceTypes, resourceValue, violationNames, size,
                startTime, endTime, random, sortingDirection, sortingColumn, visitorsCntRussiaMin, visitorsCntRussiaMax,
                visitorsCntWorldMin, visitorsCntWorldMax);

        List<Long> ids = idss.flatMap(Flux::fromIterable).collectList().block();
        if (ids != null) {
            saveErdi(unit, ids);
        }
        //trafficService.actualizeTrafficCheckUnitsCount(unit.getTraffic().getId());
        return ids;
    }

    public void saveErdi(ErdiTrafficUnit unit, List<Long> ids) {
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
        trafficService.actualizeTraffic(unit.getTraffic().getId());
    }


}
