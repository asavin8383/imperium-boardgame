package services;

import arrangement.ArrangementStatusNotification;
import arrangement.ArrangementToExecution;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitKey;
import enums.ArrangementEvents;
import events.producers.CheckUnitJobProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import model.SchedulePeriodArrangement;
import model.SchedulePeriodCheckUnit;
import model.enums.ArrangementStatus;
import model.enums.SchedulePeriodCheckUnitStatus;
import model.enums.SchedulePeriodState;
import model.enums.ScheduleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.*;
import restapi.ArrangementStatusUploader;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by san
 * Date: 04.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class RunScheduleService {

    private final ScheduleRepo scheduleRepo;
    private final SchedulePeriodRepo schedulePeriodRepo;
    private final SchedulePeriodArrangementRepo schedulePeriodArrangementRepo;
    private final SchedulePeriodCheckUnitRepo schedulePeriodCheckUnitRepo;
    private final CheckUnitJobProducer checkUnitJobProducer;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final ArrangementRepo arrangementRepo;

    private final OAuth2RestTemplate restTemplate;
    private final String DISPATCHER_POST_ARR_ENDPOINT = "/dispatcher/arrangement";

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Scheduled(cron = "${app.schedule}")
    public void runSchedule(){
        scheduleRepo.findAllByPlannedDate(LocalDate.now())
            .forEach(schedule -> schedulePeriodRepo.findAllByScheduleAndSchedulePeriodStateAndAndStartTimeBefore(schedule, SchedulePeriodState.PLANNED, LocalTime.now())
                .forEach(schedulePeriod -> {
                    schedulePeriodArrangementRepo.findAllBySchedulePeriod(schedulePeriod)
                        .forEach(schedulePeriodArrangement -> {
                            //Остановленные не запускаем
                            if(!schedulePeriodArrangement.isStopped() && sendArrangementToDispatcher(schedule.getId(), schedulePeriodArrangement)) {
                                log.debug("Запуск на выполнение schedulePeriodArrangement с ИД {}", schedulePeriodArrangement.getId());

                                Arrangement arrangement = schedulePeriodArrangement.getArrangement();
                                if(arrangement.getStatus()== ArrangementStatus.SCHEDULED){
                                    arrangement.setStatus(ArrangementStatus.RUNNING);
                                    arrangementRepo.save(arrangement);
                                    arrangementStatusUploader.changeArrangementStatus(new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.RUN));
                                }
                                List<SchedulePeriodCheckUnit> schedulePeriodCheckUnits = schedulePeriodCheckUnitRepo.findAllBySchedulePeriodArrangement(schedulePeriodArrangement);
                                schedulePeriodCheckUnits.forEach(schedulePeriodCheckUnit ->
                                                runCheckUnit(schedulePeriodCheckUnit, schedule.getId(), schedulePeriodArrangement.getArrangementId()));
                                if (!schedule.getStatus().equals(ScheduleStatus.RUNNING)) {
                                    schedule.setStatus(ScheduleStatus.RUNNING);
                                    scheduleRepo.save(schedule);
                                    log.info("Статус расписания {} сменился на 'Выполняется'", schedule.getId());
                                }
                           }

                        });
                    schedulePeriod.setSchedulePeriodState(SchedulePeriodState.STARTED);
                    schedulePeriodRepo.save(schedulePeriod);
                })
            );
    }

    private Boolean sendArrangementToDispatcher(Long scheduleId, SchedulePeriodArrangement schedulePeriodArrangement) {
        String url = UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(DISPATCHER_POST_ARR_ENDPOINT).build().toString();
        ArrangementToExecution arrangement = new ArrangementToExecution();
        arrangement.setCheckUnitsCount(schedulePeriodCheckUnitRepo.getSchedulePeriodCheckUnitCount(scheduleId, schedulePeriodArrangement.getArrangementId()));
        arrangement.setId(schedulePeriodArrangement.getArrangementId());

        ResponseEntity<String> response = restTemplate.postForEntity(url, arrangement, String.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    private void runCheckUnit(SchedulePeriodCheckUnit schedulePeriodCheckUnit, Long scheduleId, Long arrangementId){
        log.debug("Запуск чек-юнита: {} {}", schedulePeriodCheckUnit.getId(), schedulePeriodCheckUnit.getCheckUnit().getCheckUnitValue());
        if (schedulePeriodCheckUnit.getStatus().equals(SchedulePeriodCheckUnitStatus.READY)){
            int partitionId = schedulePeriodCheckUnit.getExecutionNumber().intValue();
            CheckUnitKey key = new CheckUnitKey(arrangementId, schedulePeriodCheckUnit.getId(), scheduleId);
            sendCheckUnitJobToDispatcher(
                    createCheckUnitJob(scheduleCheckUnitRepo.getOne(schedulePeriodCheckUnit.getId())),
                    key,
                    partitionId
            );
            log.debug("Чек-юнит отправлен на диспетчер. Раздел {}, значение: {} {}", partitionId, schedulePeriodCheckUnit.getId(), schedulePeriodCheckUnit.getCheckUnit().getCheckUnitValue());
        }
    }

    private void sendCheckUnitJobToDispatcher(CheckUnitJob checkUnitJob, CheckUnitKey key, int partitionId){
        checkUnitJobProducer.sendJobToDispatcher(checkUnitJob, key, partitionId);
    }

    private CheckUnitJob createCheckUnitJob(ScheduleCheckUnit scheduleCheckUnit) {
        CheckUnitJob checkUnitJob = new CheckUnitJob();
        checkUnitJob.setAccessTool(scheduleCheckUnit.getArrangement().getAccessTool());
        checkUnitJob.setCheckUnit(new CheckUnit(scheduleCheckUnit.getErdiId(), scheduleCheckUnit.getCheckUnitType(), scheduleCheckUnit.getCheckUnitValue()));
        return checkUnitJob;
    }

}
