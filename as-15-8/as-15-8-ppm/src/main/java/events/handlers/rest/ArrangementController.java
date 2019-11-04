package events.handlers.rest;

import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import enums.ExecutionStatus;
import events.producers.rest.ArrangementStatusUploader;
import events.producers.rest.CheckUnitUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;

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

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void replaceFormalTask(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        log.info("Получено мероприятие {} для включения в расписание", newArrangement.getId());
        if(arrangement != null) {
            //Удаляем старое мероприятие
            arrangementRepo.delete(arrangement);
            log.info("Мероприятие {} удалено при замене", arrangement.getId());
        }
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
            .map(checkUnit -> {
                ScheduleCheckUnit scheduleCheckUnit = new ScheduleCheckUnit();
                scheduleCheckUnit.setArrangement(arrangement);
                scheduleCheckUnit.setErdiId(checkUnit.getErdiId().toString());
                scheduleCheckUnit.setCheckUnitType(checkUnit.getType());
                scheduleCheckUnit.setCheckUnitValue(checkUnit.getValue());
                return scheduleCheckUnit;
            })
            .collect(Collectors.toList())
        );
        arrangementRepo.save(arrangement);
    }

}
