package services;

import arrangement.ArrangementStatusNotification;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Schedule;
import model.ScheduleCheckUnit;
import model.StoppedArrangement;
import model.enums.ScheduleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.*;
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
    private final StoppedArrangementsRepo stoppedArrangementsRepo;

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

    public void closeSchedulePeriodArrangements(Arrangement arrangement, Long scheduleId) {
        //Закрываем schedulePeriodArrangement у текущего расписания для данного мероприятия
        schedulePeriodArrangementRepo.findAllByScheduleAndArrangement(scheduleId, arrangement.getId())
                .forEach(schedulePeriodArrangement -> {
                    schedulePeriodArrangement.setStopped(true);
                    schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
                });
    }

    public void saveArrangementState(Arrangement arrangement, Long scheduleId, Boolean isScheduled) {
        log.info("Меняем статус мероприятию в ППМ {} из расписания {} на isScheduled = {} ", arrangement.getId(), scheduleId, isScheduled);
        arrangement.setIsScheduled(isScheduled);
        arrangementRepo.save(arrangement);
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

    public ResponseEntity stop(ArrangementStatusNotification arrangementStatusNotification) {
        Arrangement arrangement = arrangementRepo.findById(arrangementStatusNotification.getArrangementId()).orElseThrow(() ->
                new AS_15_8_PPM_Exception("Ошибка остановки мероприятия на ППМ, мероприятие не найдено в БД, id = " + arrangementStatusNotification.getArrangementId()));
        if(!arrangement.getIsScheduled()){
            return ResponseEntity.badRequest().body("Мероприятие с ID: " + arrangement.getId() + " имеет недопустимый статус для остановки, isScheduled = " + arrangement.getIsScheduled());
        } else {
            List<Schedule> runningSchedules = scheduleRepo.findByStatusAndArrangement(ScheduleStatus.RUNNING, arrangement.getId());

            if (runningSchedules.isEmpty()) {
                String errorDescription = String.format("Ошибка остановки мероприятия! Ошибка получения запущенного расписания" +
                                " с мероприятием %d, список расписаний пуст",
                        arrangement.getId());

                log.error(errorDescription);
                return ResponseEntity.badRequest().body(errorDescription);
            }

            runningSchedules.forEach(schedule -> {
                log.info("Мероприятие {} из расписания {} остановлено, закрываем schedulePeriodArrangements", arrangement.getId(), schedule.getId());

                closeSchedulePeriodArrangements(arrangement, schedule.getId());
                saveArrangementState(arrangement, schedule.getId(), false);
                madeSheduleIsStopped(schedule, true);
                finishSchedule(arrangementStatusNotification);
                fillStoppedArrangementsTable(arrangementStatusNotification, schedule);
            });
            return ResponseEntity.ok().build();
        }
    }

    public ResponseEntity finishSchedule(ArrangementStatusNotification arrangementStatusNotification) {
        Arrangement arrangement = arrangementRepo.findById(arrangementStatusNotification.getArrangementId()).orElseThrow(() ->
                new AS_15_8_PPM_Exception("Невозможно завершить расписание, мероприятие не существует id = " + arrangementStatusNotification.getArrangementId()));
        //Проверка, не нужно ли закрыть расписание
        refreshStoppedArrangement(arrangement);
        arrangement.setIsScheduled(false);
        arrangementRepo.save(arrangement);

        try {
            scheduleRepo
                    .findByArrangement(arrangement.getId())
                    .forEach(this::checkAndCloseSchedule);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex);
        }
    }

    private void checkAndCloseSchedule(Schedule schedule) {
        log.info("Проверка, необходимо ли закрыть расписание, id = ", schedule.getId());
        boolean needToClose = arrangementRepo.findAllBySchedule(schedule.getId())
                .stream()
                .noneMatch(Arrangement::getIsScheduled);
        if (needToClose) {
            schedule.setStatus(ScheduleStatus.FINISHED);
            scheduleRepo.save(schedule);
            log.info("Статус расписания с ИД: {} сменился на 'ЗАКРЫТО'", schedule.getId());
        } else {
            log.info("Статус расписания не изменился! id = ", schedule.getId());
        }
    }

    private void madeSheduleIsStopped(Schedule schedule, Boolean containsStoppedArrangements) {
        schedule.setContainsStoppedArrangements(containsStoppedArrangements);
        scheduleRepo.save(schedule);
    }

    private void fillStoppedArrangementsTable(ArrangementStatusNotification arrangementStatusNotification, Schedule schedule) {
        StoppedArrangement stoppedArrangement = new StoppedArrangement();
        stoppedArrangement.setArrangementId(arrangementStatusNotification.getArrangementId());
        stoppedArrangement.setStoppingReason(arrangementStatusNotification.getEvent().getDescription());
        stoppedArrangement.setCompletionPerscent(arrangementStatusNotification.getCompletionPerscent());
        stoppedArrangement.setSchedule(schedule);
        stoppedArrangementsRepo.save(stoppedArrangement);
    }
}
