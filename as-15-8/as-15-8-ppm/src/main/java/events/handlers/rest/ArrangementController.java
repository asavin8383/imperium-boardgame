package events.handlers.rest;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import common.SchedulerProperties;
import enums.AccessToolUnit;
import enums.ArrangementEvents;
import enums.Protocol;
import events.producers.rest.ArrangementStatusUploader;
import events.producers.rest.CheckUnitUploader;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;
import repositories.DomainMaskItemRepo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 31.10.2019
 */
@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final CheckUnitUploader checkUnitUploader;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final SchedulerProperties schedulerProperties;
    private final DomainMaskItemRepo domainMaskItemRepo;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateArrangement(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        log.info("Получено мероприятие {} для включения в расписание", newArrangement.getId());
        if(arrangement != null) {
            //Удаляем старое мероприятие
            arrangementRepo.delete(arrangement);
            log.info("Мероприятие {} удалено при замене", arrangement.getId());
        }
        if(newArrangement.getPlannedStartTime()==null){
            newArrangement.setPlannedStartTime(LocalTime.of(9,0));
        }
        if(newArrangement.getPlannedEndTime()==null){
            newArrangement.setPlannedEndTime(LocalTime.of(18,0));
        }
        newArrangement.setMaxWorkersCount(schedulerProperties.getTotalWorkersCount());
        arrangementRepo.save(newArrangement);
        log.info("Мероприятие {} записано в БД", newArrangement.getId());
        getAndSaveArrangementCheckUnits(newArrangement);
        log.info("Мероприятие {} готово к включению в расписание", newArrangement.getId());
        ArrangementStatusNotification arrangementStatusNotification = new ArrangementStatusNotification(newArrangement.getId(), ArrangementEvents.FILL);
        arrangementStatusUploader.changeArrangementStatus(arrangementStatusNotification);
    }

    private void getAndSaveArrangementCheckUnits(Arrangement arrangement){
        arrangement.getScheduleCheckUnits().addAll(
        checkUnitUploader.getCheckUnitsByArrangementId(arrangement.getId())
            .stream()
            .flatMap(checkUnit -> createCheckUnits(arrangement, checkUnit).stream())
            .collect(Collectors.toList())
        );
        arrangementRepo.save(arrangement);
    }

    private List<ScheduleCheckUnit> createCheckUnits(Arrangement arrangement, CheckUnit checkUnit){
        List<ScheduleCheckUnit> scheduleCheckUnits = new ArrayList<>();
        switch (checkUnit.getType()){
            case DOMAIN: {
                String unitName = schedulerProperties.getAccessTools().get(arrangement.getAccessTool());
                if(unitName == null) {
                    throw AS_15_8_PPM_Exception.logAndGet(log, "Ошибка получения ПС/ПАСД по ключу: " + arrangement.getAccessTool());
                }
                AccessToolUnit accessToolUnit = AccessToolUnit.fromPropertyKey(unitName);
                boolean isPS = accessToolUnit == AccessToolUnit.SEARCH_SYSTEM ||
                        accessToolUnit == AccessToolUnit.GOOGLE_API;
                if (isPS) {
                    scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getErdiId(), CheckUnitType.URL, checkUnit.getValue()));
                } else {
                    for(Protocol value: Protocol.values()){
                        scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getErdiId(), CheckUnitType.URL, value.getProtocol() + checkUnit.getValue()));
                    }
                }
                return scheduleCheckUnits;
            }
            case DOMAIN_MASK: {
                domainMaskItemRepo.findAllByDomainMask(checkUnit.getValue())
                    .forEach(domainMaskItem -> scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getErdiId(), CheckUnitType.URL, domainMaskItem.getDomainMaskItem())));
                return scheduleCheckUnits;
            }
            case IP_V4: {
                if (checkUnit.getValue().matches("\\\\\\d{1,3}$")){
                    scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getErdiId(), CheckUnitType.IP_V4_SUBNET, checkUnit.getValue()));
                } else {
                    scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getErdiId(), checkUnit.getType(), checkUnit.getValue()));
                }
                return scheduleCheckUnits;
            }
            default: {
                scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getErdiId(), checkUnit.getType(), checkUnit.getValue()));
                return scheduleCheckUnits;
            }
        }
    }

    private ScheduleCheckUnit createCheckUnit(Arrangement arrangement, Long erdiId, CheckUnitType checkUnitType, String checkUnitValue){
        ScheduleCheckUnit scheduleCheckUnit = new ScheduleCheckUnit();
        scheduleCheckUnit.setArrangement(arrangement);
        scheduleCheckUnit.setErdiId(erdiId);
        scheduleCheckUnit.setCheckUnitType(checkUnitType);
        scheduleCheckUnit.setCheckUnitValue(checkUnitValue);
        return scheduleCheckUnit;
    }

}
