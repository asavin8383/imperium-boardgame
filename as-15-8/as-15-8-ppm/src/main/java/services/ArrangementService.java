package services;

import arrangement.ArrangementStatusNotification;
import enums.ExecutionStatus;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Schedule;
import model.ScheduleCheckUnit;
import model.enums.ArrangementStatus;
import model.enums.ScheduleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.ArrangementRepo;
import repositories.ScheduleCheckUnitRepo;
import repositories.SchedulePeriodArrangementRepo;
import repositories.ScheduleRepo;
import webClients.DispatcherWebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by san
 * Date: 05.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final ArrangementRepo arrangementRepo;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final SchedulePeriodArrangementRepo schedulePeriodArrangementRepo;
    private final ScheduleRepo scheduleRepo;
    private final DispatcherWebClient dispatcherWebClient;
    private final OAuth2RestTemplate restTemplate;
    private final String DISPATCHER_STATUS_ENDPOINT = "/dispatcher/arrangements/status";
    private final String PPT_STATUS_ENDPOINT = "/ppt/arrangements/status";

    public void updateArrangementPlanInfo(Arrangement arrangement){
        if(arrangement.getPlannedStartTime()==null || arrangement.getPlannedEndTime() == null){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка изменения планового времени мероприятия. Некорректные входные параметры: дата начала - %s, дата окончания - %s", arrangement.getPlannedStartTime(), arrangement.getPlannedEndTime()));
        }
        Arrangement updateArrangement =
                arrangementRepo.findById(arrangement.getId())
                        .orElseThrow(() -> new AS_15_8_PPM_Exception("Ошибка изменения планового времени мероприятия. Мероприятие с ИД: " + arrangement.getId() + " не было найдено в БД"));
        updateArrangement.setPlannedStartTime(arrangement.getPlannedStartTime());
        updateArrangement.setPlannedEndTime(arrangement.getPlannedEndTime());
        arrangementRepo.save(updateArrangement);
    }

    public Page<Arrangement> findPage(PageRequest page){
        return arrangementRepo.findAllAvailableArrangements(page);
    }

    List<Arrangement> findAllAvailableArrangements(){
        return arrangementRepo.findAllAvailableArrangements();
    }

    Map<Arrangement, TreeSet<ScheduleCheckUnit>> getArrangementCheckUnits(List<Long> arrangementIds, LocalDate plannedDate){
        Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits = new HashMap<>();
        arrangementIds.forEach(arrangementId -> {
            Arrangement arrangement = arrangementRepo.findById(arrangementId)
                    .orElseThrow(() -> new AS_15_8_PPM_Exception("Ошибка создания расписания! Мероприятие не было найдено по ID: " + arrangementId));


            //Проверяем, что мероприятие не просрочено
            long timeDuration = ChronoUnit.SECONDS.between(
                    LocalDateTime.of(plannedDate, arrangement.getPlannedStartTime()),
                    LocalDateTime.now());

            LocalTime startTime = arrangement.getPlannedStartTime().plusSeconds(timeDuration);
            LocalTime endTime = arrangement.getPlannedEndTime().plusSeconds(timeDuration);
            if(timeDuration > 0) {
                arrangement.setPlannedStartTime(startTime);
                arrangement.setPlannedEndTime(endTime.isAfter(startTime) ? endTime : LocalTime.MIDNIGHT.minusSeconds(1));
            }

            TreeSet<ScheduleCheckUnit> arrangementResults = new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
            arrangementResults.addAll(scheduleCheckUnitRepo.findAllByArrangementAndFinished(arrangement, false));
            arrangementCheckUnits.put(arrangement, arrangementResults);
        });
        return arrangementCheckUnits;
    }

    @Transactional
    public void refreshStoppedArrangement(Arrangement arrangement){
        //Меняем статус для чек-юнитов, завершенных на диспетчере
        scheduleCheckUnitRepo.changeFinished(
            arrangement,
            dispatcherWebClient.getJobIdsFromDispatcher(arrangement.getId()),
            true
        );
    }

    /*public boolean notifyDispatcher(Arrangement arrangement, Long scheduleId) {
        log.info("Отправляем сигнал на закрытие мероприятия {} из расписания {} диспетчеру", arrangement.getId(), scheduleId);
        if (!sendStatusChangeToDispatcher(arrangement.getId(), scheduleId, ExecutionStatus.STOPPED)){
            log.error("Ошибка остановки мероприятия {} из расписания {}", arrangement.getId(), scheduleId);
            //return ResponseEntity.badRequest().body(String.format("Ошибка остановки мероприятия %d из расписания %d", arrangement.getId(), scheduleId));
            return false;
        } return true;
    }*/

    public void closeSchedulePeriodArrangements(Arrangement arrangement, Long scheduleId) {
        //Закрываем schedulePeriodArrangement у текущего расписания для данного мероприятия
        schedulePeriodArrangementRepo.findAllByScheduleAndArrangement(scheduleId, arrangement.getId())
                .forEach(schedulePeriodArrangement -> {
                    schedulePeriodArrangement.setStopped(true);
                    schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
                });
    }

    public void saveArrangementStatus(Arrangement arrangement, Long scheduleId, ArrangementStatus status) {
        log.info("Меняем статус мероприятию в ППТ {} из расписания {} на {} ", arrangement.getId(), scheduleId, status);
        arrangement.setStatus(status);
        arrangementRepo.save(arrangement);
    }

    public boolean sendStatusChangeToDispatcher(Long arrangementId, Long scheduleId, ExecutionStatus executionStatus){
        log.info("Отправляем сигнал на изменение статуса мероприятия {} из расписания {} диспетчеру, статус {} ", arrangementId, scheduleId, executionStatus);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExecutionStatus> statusEntitry = new HttpEntity<>(executionStatus, headers);

        log.info("Отправка сообщения на изменение статуса мероприятия мероприятия {} из расписания {} диспетчеру, статус {} ", arrangementId, scheduleId, executionStatus);
        try {
            restTemplate.exchange(
                    UriComponentsBuilder
                            .fromHttpUrl(gatewayUrl)
                            .path(DISPATCHER_STATUS_ENDPOINT)
                            .queryParam("arrangementId", arrangementId)
                            .queryParam("version", scheduleId)
                            .build().toString(),
                    HttpMethod.POST,
                    statusEntitry,
                    Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Ошибка отправки сообщения на изменение статуса мероприятия {} из расписания {} диспетчеру, статус {} ", arrangementId, scheduleId, executionStatus);
            log.error("Информация об ошибке", ex);
            return false;
        }
        log.info("Сообщение на изменение статуса мероприятия {} из расписания {} успешно отправлено диспетчеру, статус {} ", arrangementId, scheduleId, executionStatus);
        return true;
    }

    public boolean notifyPPT(ArrangementStatusNotification arrangementStatusNotification){

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

    public boolean isScheduleAvailable(Arrangement arrangement, List<Schedule> schedules) {
        if(schedules.size() != 1){
            return false;
        } return true;
    }

    public Long getRunningScheduleId(Long arrangementId) {
        List<Schedule> schedules = scheduleRepo.findByStatusAndArrangement(ScheduleStatus.RUNNING, arrangementId);
        return  schedules.get(0).getId();
    }
}
