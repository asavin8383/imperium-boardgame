package controllers;

import arrangement.ArrangementStatusNotification;
import arrangement.ArrangementToPPM;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import common.SchedulerProperties;
import enums.AccessToolUnit;
import enums.ArrangementEvents;
import enums.ExecutionStatus;
import enums.Protocol;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Schedule;
import model.ScheduleCheckUnit;
import model.enums.ArrangementStatus;
import model.enums.ScheduleStatus;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;
import repositories.ScheduleRepo;
import restapi.ArrangementStatusUploader;
import restapi.pod.DomainMaskUploader;
import restapi.ppt.PptRestApi;
import services.ArrangementService;
import services.ScheduleService;
import webClients.PPT_WebClient;

import javax.transaction.Transactional;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ArrangementService arrangementService;
    private final PPT_WebClient pptWebClient;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final SchedulerProperties schedulerProperties;
    private final DomainMaskUploader domainMaskUploader;
    private final ScheduleRepo scheduleRepo;
    private final ScheduleService scheduleService;
    private final PptRestApi pptRestApi;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> updateArrangement(@RequestBody ArrangementToPPM arrToPPM, @RequestParam("id") Arrangement arrangement) {
        Arrangement newArrangement = convertArrangement(arrToPPM);
        try {
            log.info("Получено мероприятие {} для включения в расписание", newArrangement.getId());
            if (arrangement != null) {
                //Удаляем старое мероприятие
                arrangementRepo.delete(arrangement);
                log.info("Мероприятие {} удалено при замене", arrangement.getId());
            }
            newArrangement.setStatus(ArrangementStatus.NEW);
            if (newArrangement.getPlannedStartTime() == null) {
                newArrangement.setPlannedStartTime(LocalTime.of(9, 0));
            }
            if (newArrangement.getPlannedEndTime() == null) {
                newArrangement.setPlannedEndTime(LocalTime.of(18, 0));
            }
            newArrangement.setMaxWorkersCount(schedulerProperties.getTotalWorkersCount());
            getAndSaveArrangementCheckUnits(newArrangement);
            log.info("Мероприятие {} готово к включению в расписание", newArrangement.getId());
            ArrangementStatusNotification arrangementStatusNotification = new ArrangementStatusNotification(newArrangement.getId(), ArrangementEvents.FILL);
            arrangementStatusUploader.changeArrangementStatus(arrangementStatusNotification);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            log.error("Ошибка при формировании мероприятия", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Ошибка при формировании мероприятия", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    private Arrangement convertArrangement(ArrangementToPPM arrToPPM) {
        Arrangement arrangement = new Arrangement();
        arrangement.setId(arrToPPM.getId());
        arrangement.setTitle(arrToPPM.getTitle());
        arrangement.setCreationDate(arrToPPM.getCreationDate());
        arrangement.setPlannedStartTime(arrToPPM.getPlannedStartTime());
        arrangement.setPlannedEndTime(arrToPPM.getPlannedEndTime());
        arrangement.setMaxWorkersCount(arrToPPM.getMaxWorkersCount());
        arrangement.setAccessTool(arrToPPM.getAccessTool());
        return arrangement;
    }

    @PutMapping(value = "/close", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void updateArrangementStatus(@RequestParam("id") Arrangement arrangement, @RequestBody ArrangementStatusNotification notification){
        if (arrangement == null){
            throw new AS_15_8_PPM_Exception("Ошибка закрытия мероприятия! Мероприятие не было найдено в БД");
        }

        if (notification == null){
            throw new AS_15_8_PPM_Exception("Ошибка закрытия мероприятия! ArrangementStatusNotification is null");
        }
        if (arrangementService.notifyPPT(notification)) {
            if (notification.getEvent().equals(ArrangementEvents.STOP)) {
                arrangement.setStatus(ArrangementStatus.STOPPED);
            } else if (notification.getEvent().equals(ArrangementEvents.STOP_BY_MAX_CHECK_UNITS_COUNT)) {
                arrangement.setStatus(ArrangementStatus.STOPPED_BY_MAX_CHECK_UNITS_COUNT);
            } else if (notification.getEvent().equals(ArrangementEvents.STOP_BY_SERVICE_MODE)) {
                arrangement.setStatus(ArrangementStatus.STOPPED_BY_SERVICE_MODE);
            } else arrangement.setStatus(ArrangementStatus.FINISHED);
        }
        arrangementService.refreshStoppedArrangement(arrangement);
        arrangementRepo.save(arrangement);
            //Проверка, не нужно ли закрыть расписание
            scheduleRepo
                .findByArrangement(arrangement.getId())
                .forEach(scheduleService::checkAndCloseSchedule);
    }


    @PutMapping(value = "/stop")
    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    @Transactional
    public ResponseEntity stopArrangement(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        if(arrangement.getStatus()!= ArrangementStatus.RUNNING){
            return ResponseEntity.badRequest().body("Мероприятие с ID: " + arrangement.getId() + " имеет недопустимый статус для остановки: " + arrangement.getStatus());
        } else {
            List<Schedule> schedules = scheduleRepo.findByStatusAndArrangement(ScheduleStatus.RUNNING, arrangement.getId());
            if (arrangementService.isScheduleAvailable(arrangement, schedules)) {
                Long scheduleId = arrangementService.getRunningScheduleId(arrangement.getId());
                if (!arrangementService.sendStatusChangeToDispatcher(arrangement.getId(), scheduleId, ExecutionStatus.STOPPED))
                    return ResponseEntity.badRequest().body(String.format("Ошибка остановки мероприятия %d из расписания %d", arrangement.getId(), scheduleId));

                log.info("Мероприятие {} из расписания {} остановлено, закрываем schedulePeriodArrangements", arrangement.getId(), scheduleId);

                arrangementService.closeSchedulePeriodArrangements(arrangement, scheduleId);
                arrangementService.saveArrangementStatus(arrangement, scheduleId, ArrangementStatus.STOPPING);

                log.info("Отправляем в ППТ запрос смены статуса мероприятию {} из расписания {} на STOPPING", arrangement.getId(), scheduleId);
                arrangementService.notifyPPT(new ArrangementStatusNotification(
                        arrangement.getId(),
                        ArrangementEvents.PREPARE_TO_STOP
                ));
                return ResponseEntity.ok().build();
            } else {
                String errorDescription = String.format("Ошибка остановки мероприятия! Ошибка получения запущенного расписания" +
                        " с мероприятием %d, список расписаний либо пуст либо содержит более 1 элемента: %s",
                        arrangement.getId(),
                        schedules.toString());
                log.error(errorDescription);
                return ResponseEntity.badRequest().body(errorDescription);
            }
        }
    }

    @PutMapping(value = "/finish")
    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    @Transactional
    public ResponseEntity finishArrangement(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        if (arrangement.getStatus() != ArrangementStatus.STOPPED
                && arrangement.getStatus()!= ArrangementStatus.STOPPED_BY_MAX_CHECK_UNITS_COUNT
                && arrangement.getStatus()!= ArrangementStatus.STOPPED_BY_SERVICE_MODE){
            return ResponseEntity.badRequest().body("Мероприятие с ID: " + arrangement.getId() + " имеет недопустимый статус для завершения: " + arrangement.getStatus());
        } else {
            if (arrangementService.sendStatusChangeToDispatcher(arrangement.getId(), null, ExecutionStatus.FINISHED)) {
                arrangementService.notifyPPT(new ArrangementStatusNotification(
                        arrangement.getId(),
                        ArrangementEvents.FINISH));
                arrangementService.saveArrangementStatus(arrangement, null, ArrangementStatus.FINISHED);
                return ResponseEntity.ok().build();

            } return ResponseEntity.badRequest().body("Мероприятие не переведено в статус FINISHED");
        }
    }

    /**
     * Обновление статусов чек-юнитов для возможности повторного запуска мероприятия
     * @param arrangement мероприятие, которое требуется запустить повторно
     * @return Ответ от сервиса
     */
    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    @PutMapping("/refresh")
    public ResponseEntity refreshArrangement(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        arrangementService.refreshStoppedArrangement(arrangement);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    @GetMapping()
    public ResponseEntity getArrangement(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(arrangement);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    @GetMapping("/schedule_id")
    public ResponseEntity getSchedule(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        Long scheduleId = scheduleRepo.findMaxScheduleIdByArrangement(arrangement.getId());
        if (scheduleId != null)
            return ResponseEntity.ok().body(scheduleId);
        else return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    @GetMapping("/execution_status_by_id")
    public String getStatusById(@RequestParam("id") Long id) {
        return pptRestApi.fetchExecutionStatus(id).getDescription();
    }

    private void getAndSaveArrangementCheckUnits(Arrangement arrangement){
        arrangement.getScheduleCheckUnits().addAll(
                Objects.requireNonNull(
                        pptWebClient
                                .getFromPPT(arrangement.getId())
                                .stream()
                                .flatMap(checkUnit -> createCheckUnits(arrangement, checkUnit).stream())
                                .collect(Collectors.toList())
                )
        );
        log.info("Получен список чек-юнитов мероприятия {} от ППТ", arrangement.getId());
        if(arrangement.getScheduleCheckUnits().size() > 0){
            arrangementRepo.save(arrangement);
            log.info("Список чек-юнитов мероприятия {} сохранен", arrangement.getId());
        } else {
            throw new IllegalStateException("Ошибка формирования мероприятия " + arrangement.getId() +
                    "  в ППМ. Трафик не содержит значений для проверки");
        }
    }

    private List<ScheduleCheckUnit> createCheckUnits(Arrangement arrangement, CheckUnit checkUnit){
        String unitName = schedulerProperties.getAccessTools().get(arrangement.getAccessTool());
        if(Strings.isEmpty(unitName))
            throw new AS_15_8_PPM_Exception("Ошибка при получении конфигурации робота " + arrangement.getAccessTool() + ". Проверьте и обновите конфигурацию ППМ");
        AccessToolUnit accessToolUnit = AccessToolUnit.fromPropertyKey(unitName);
        boolean isPS = accessToolUnit == AccessToolUnit.SEARCH_SYSTEM ||
                accessToolUnit == AccessToolUnit.GOOGLE_API;
        List<ScheduleCheckUnit> scheduleCheckUnits = new ArrayList<>();
        switch (checkUnit.getType()){
            case DOMAIN: {
                addDomainToScheduleCheckUnits(isPS, scheduleCheckUnits, checkUnit.getContentId(), checkUnit.getValue(), arrangement);
                return scheduleCheckUnits;
            }
            case DOMAIN_MASK: {
                domainMaskUploader.getDomains(checkUnit.getValue())
                        .forEach(domain ->
                                addDomainToScheduleCheckUnits(isPS, scheduleCheckUnits, checkUnit.getContentId(), domain, arrangement));
                return scheduleCheckUnits;
            }
            case IP_V4:
            case IP_V6:
            case IP_V4_SUBNET:
            case IP_V6_SUBNET:
            {
                if (!isPS){
                    scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getContentId(), checkUnit.getType(), checkUnit.getValue()));
                }
                return scheduleCheckUnits;
            }
            default: {
                scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getContentId(), checkUnit.getType(), checkUnit.getValue()));
                return scheduleCheckUnits;
            }
        }
    }

    private void addDomainToScheduleCheckUnits(boolean isPS, List<ScheduleCheckUnit> scheduleCheckUnits, Long contentId, String checkUnitValue, Arrangement arrangement){
        if (isPS) {
            scheduleCheckUnits.add(createCheckUnit(arrangement, contentId, CheckUnitType.DOMAIN, checkUnitValue));
        } else {
            for(Protocol value: Protocol.values()){
                scheduleCheckUnits.add(createCheckUnit(arrangement, contentId, CheckUnitType.URL, value.getProtocol() + checkUnitValue));
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
