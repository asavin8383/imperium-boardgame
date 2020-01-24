package controllers;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import common.SchedulerProperties;
import enums.AccessToolUnit;
import enums.ArrangementEvents;
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
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.ArrangementRepo;
import repositories.ScheduleCheckUnitRepo;
import repositories.SchedulePeriodArrangementRepo;
import repositories.ScheduleRepo;
import restapi.ArrangementStatusUploader;
import restapi.pod.DomainMaskUploader;
import services.ScheduleService;
import webClients.DispatcherWebClient;
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
    private final PPT_WebClient pptWebClient;
    private final DispatcherWebClient dispatcherWebClient;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final SchedulerProperties schedulerProperties;
    private final DomainMaskUploader domainMaskUploader;
    private final ScheduleRepo scheduleRepo;
    private final ScheduleService scheduleService;
    private final OAuth2RestTemplate restTemplate;
    private final SchedulePeriodArrangementRepo schedulePeriodArrangementRepo;

    @Value("${gateway.url}")
    private String gatewayUrl;
    private final String PPT_STATUS_ENDPOINT = "/ppt/arrangements/status";
    private final String DISPATCHER_STOP_ENDPOINT = "/dispatcher/stop";
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> updateArrangement(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        try {
            log.info("Получено мероприятие {} для включения в расписание", newArrangement.getId());
            if (arrangement != null) {
                //Удаляем старое мероприятие
                arrangementRepo.delete(arrangement);
                log.info("Мероприятие {} удалено при замене", arrangement.getId());
            }
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

    @PutMapping(value = "/close", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void updateArrangementStatus(@RequestParam("id") Arrangement arrangement, @RequestBody ArrangementStatusNotification notification){
        if (arrangement == null){
            throw new AS_15_8_PPM_Exception("Ошибка закрытия мероприятия! Мероприятие не было найдено в БД");
        }

        if (notification == null){
            throw new AS_15_8_PPM_Exception("Ошибка закрытия мероприятия! ArrangementStatusNotification is null");
        }

        if (sendToPPT(notification)) {
            arrangement.setStatus(ArrangementStatus.FINISHED);
            arrangementRepo.save(arrangement);
            //Проверка, не нужно ли закрыть расписание
            scheduleRepo
                .findByArrangement(arrangement.getId())
                .forEach(scheduleService::checkAndCloseSchedule);
        }
    }

    @PutMapping(value = "/stop")
    @Transactional
    public ResponseEntity stopArrangement(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        if(arrangement.getStatus()!=ArrangementStatus.RUNNING){
            return ResponseEntity.badRequest().body("Мероприятие с ID: " + arrangement.getId() + " имеет недопустимый статус для остановки: " + arrangement.getStatus());
        } else {
            List<Schedule> schedules = scheduleRepo.findByStatusAndArrangement(ScheduleStatus.RUNNING, arrangement.getId());
            if(schedules.size() != 1){
                String errorDescription = String.format("Ошибка остановки мероприятия! Ошибка получения запущенного расписания с мероприятием %d, список расписаний либо пуст либо содержит более 1 элемента: %s", arrangement.getId(), schedules.toString());
                log.error(errorDescription);
                return ResponseEntity.badRequest().body(errorDescription);
            }

            //Закрываем schedulePeriodArrangement у текущего расписания для данного мероприятия
            schedulePeriodArrangementRepo.findAllByScheduleAndArrangement(schedules.get(0).getId(), arrangement.getId())
                .forEach(schedulePeriodArrangement -> {
                    schedulePeriodArrangement.setStopped(true);
                    schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
                });
            //Меняем статус мероприятию
            arrangement.setStatus(ArrangementStatus.STOPPED);
            arrangementRepo.save(arrangement);
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Обновление статусов чек-юнитов для возможности повторного запуска мероприятия
     * @param arrangement мероприятие, которое требуется запустить повторно
     * @return Ответ от сервиса
     */
    @PutMapping("/refresh")
    public ResponseEntity refreshArrangement(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            return ResponseEntity.noContent().build();
        }
        //Меняем статус для чек-юнитов, завершенных на диспетчере
        scheduleCheckUnitRepo.changeFinished(
            arrangement,
            dispatcherWebClient.getJobIdsFromDispatcher(arrangement.getId()),
            true
        );
        return ResponseEntity.ok().build();
    }

    private boolean sendStopSignalToDispatcher(Long arrangementId, Long scheduleId){

        log.info("Отправка сообщения на остановку мероприятия {} из расписания {} диспетчеру", arrangementId, scheduleId);
        try {
            restTemplate.exchange(
                UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(DISPATCHER_STOP_ENDPOINT)
                    .queryParam("arrangementId", arrangementId)
                    .queryParam("version", scheduleId)
                    .build().toString(),
                HttpMethod.POST,
                null,
                Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Ошибка отправки сообщения на остановку мероприятия {} из расписания {} диспетчеру", arrangementId, scheduleId);
            return false;
        }
        log.info("Сообщение на остановку мероприятия {} из расписания {} успешно отправлено диспетчеру", arrangementId, scheduleId);
        return true;
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

    private boolean sendToPPT(ArrangementStatusNotification arrangementStatusNotification){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(arrangementStatusNotification);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {} в ППТ", arrangementStatusNotification.getArrangementId());
        try {
            restTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_STATUS_ENDPOINT).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            //throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка отправки сообщения с изменением статуса мероприятия %d в ППТ, код возврата %s", arrangementStatusNotification.getArrangementId(), ex.getStatusCode()));
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия {} в ППТ, код возврата {}", arrangementStatusNotification.getArrangementId(), ex.getStatusCode());
            return false;
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено в ППТ", arrangementStatusNotification.getArrangementId());
        return true;
    }

}
