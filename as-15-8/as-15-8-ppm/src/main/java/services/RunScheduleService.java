package services;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import enums.ArrangementEvents;
import events.producers.kafka.CheckUnitJobProducer;
import events.producers.rest.ArrangementStatusUploader;
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
                            arrangementStatusUploader.changeArrangementStatus(new ArrangementStatusNotification(schedulePeriodArrangement.getArrangement().getId(), ArrangementEvents.RUN));
                            schedulePeriodCheckUnitRepo.findAllBySchedulePeriodArrangement(schedulePeriodArrangement)
                                    .forEach(this::runCheckUnit);
                        });
                    schedulePeriod.setSchedulePeriodState(SchedulePeriodState.STARTED);
                    schedulePeriodRepo.save(schedulePeriod);
                }));
    }

    private void runCheckUnit(SchedulePeriodCheckUnit schedulePeriodCheckUnit){
        if (schedulePeriodCheckUnit.getStatus().equals(SchedulePeriodCheckUnitStatus.READY)){
            String key = schedulePeriodCheckUnit.getSchedulePeriodArrangement().getArrangement().getId().toString() + "_" + schedulePeriodCheckUnit.getExecutionNumber();
            sendCheckUnitJobToDispatcher(createCheckUnitJob(scheduleCheckUnitRepo.getOne(schedulePeriodCheckUnit.getId())), key);
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
