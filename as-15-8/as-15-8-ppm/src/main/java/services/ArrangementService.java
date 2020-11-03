package services;

import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepo;
import repositories.ScheduleCheckUnitRepo;
import webClients.DispatcherWebClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.google.common.collect.Lists;

/**
 * Created by san
 * Date: 05.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementService {

    private final ArrangementRepo arrangementRepo;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final DispatcherWebClient dispatcherWebClient;

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
        List<Long> dispatcherJobIds = dispatcherWebClient.getJobIdsFromDispatcher(arrangement.getId());
        List<List<Long>> batchedJobIds = Lists.partition(dispatcherJobIds, 10000);
        batchedJobIds.forEach(batch -> {
            scheduleCheckUnitRepo.changeFinished(
                    arrangement,
                    batch,
                    true
            );
        });

        log.info("Статусы у ScheduleCheckUnit обновлены успешно");
    }
}
