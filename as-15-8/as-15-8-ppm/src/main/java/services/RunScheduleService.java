package services;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import enums.ArrangementEvents;
import events.producers.CheckUnitJobProducer;
import restapi.ArrangementStatusUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ScheduleCheckUnit;
import model.SchedulePeriodCheckUnit;
import model.SchedulePeriodCheckUnitStatus;
import model.ScheduleStatus;
import model.enums.SchedulePeriodState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.*;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;

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


    @Scheduled(cron = "${app.schedule}")
    public void runSchedule(){
        scheduleRepo.findAllByPlannedDateAndStatus(LocalDate.now(), ScheduleStatus.PLANNED)
            .forEach(schedule -> schedulePeriodRepo.findAllByScheduleAndSchedulePeriodStateAndAndStartTimeBefore(schedule, SchedulePeriodState.CREATED, LocalTime.now())
                .forEach(schedulePeriod -> {
                    schedulePeriodArrangementRepo.findAllBySchedulePeriod(schedulePeriod)
                        .forEach(schedulePeriodArrangement ->
                        {
                            log.debug("Запуск на выполнение schedulePeriodArrangement с ИД {}", schedulePeriodArrangement.getId());
                            arrangementStatusUploader.changeArrangementStatus(new ArrangementStatusNotification(schedulePeriodArrangement.getArrangement().getId(), ArrangementEvents.RUN));
                            schedulePeriodCheckUnitRepo.findAllBySchedulePeriodArrangement(schedulePeriodArrangement)
                                    .forEach(this::runCheckUnit);
                        });
                    schedulePeriod.setSchedulePeriodState(SchedulePeriodState.STARTED);
                    schedulePeriodRepo.save(schedulePeriod);
                }));
    }

    private void runCheckUnit(SchedulePeriodCheckUnit schedulePeriodCheckUnit){
        log.debug("Запуск чек-юнита: {} {}", schedulePeriodCheckUnit.getId(), schedulePeriodCheckUnit.getCheckUnit().getCheckUnitValue());
        if (schedulePeriodCheckUnit.getStatus().equals(SchedulePeriodCheckUnitStatus.READY)){
            String key = new StringBuilder()
                    .append(schedulePeriodCheckUnit.getSchedulePeriodArrangement().getArrangement().getId())
                    .append("_")
                    .append(schedulePeriodCheckUnit.getExecutionNumber())
                    .toString();
            sendCheckUnitJobToDispatcher(createCheckUnitJob(scheduleCheckUnitRepo.getOne(schedulePeriodCheckUnit.getId())), key);
            log.debug("Чек-юнит отправлен на диспетчер. Ключ: {} , значение: {} {}", key, schedulePeriodCheckUnit.getId(), schedulePeriodCheckUnit.getCheckUnit().getCheckUnitValue());
        }
    }

    private void sendCheckUnitJobToDispatcher(CheckUnitJob checkUnitJob, String key){
        checkUnitJobProducer.sendJobToDispatcher(checkUnitJob, key);
    }

    private CheckUnitJob createCheckUnitJob(ScheduleCheckUnit scheduleCheckUnit) {
        CheckUnitJob checkUnitJob = new CheckUnitJob();
        checkUnitJob.setArrangementId(scheduleCheckUnit.getArrangement().getId());
        checkUnitJob.setAccessTool(scheduleCheckUnit.getArrangement().getAccessTool());
        checkUnitJob.setCheckUnit(new CheckUnit(scheduleCheckUnit.getErdiId(), scheduleCheckUnit.getCheckUnitType(), scheduleCheckUnit.getCheckUnitValue()));
        return checkUnitJob;
    }

}
