package services;

import arrangement.ArrangementStatusNotification;
import common.SchedulerProperties;
import enums.ArrangementEvents;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.*;
import restapi.ArrangementStatusUploader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScheduleService {

    private final ScheduleCreationService scheduleCreationService;
    private final ArrangementService arrangementService;
    private final ScheduleRepo scheduleRepo;
    private final SchedulePeriodArrangementRepo schedulePeriodArrangementRepo;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final SchedulePeriodCheckUnitRepo schedulePeriodCheckUnitRepo;
    private final ArrangementRepo arrangementRepo;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final SchedulePeriodRepo schedulePeriodRepo;
    private final SchedulerProperties schedulerProperties;

    public void deleteSchedule(Schedule schedule){
        List<Arrangement> arrangements = arrangementRepo.findAllBySchedule(schedule.getId());
        scheduleRepo.delete(schedule);
        arrangements.forEach(arrangement ->
                arrangementStatusUploader.changeArrangementStatus(
                        new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.SCHEDULE_ROLLBACK)
                )
        );
    }

    /**
     * Сохранение ресурсов каждого мероприятия по периодам расписания
     * Установка статуса PLANNED расписанию
     * @return Запланированное расписание
     */
    @Transactional
    public Schedule planSchedule(Schedule schedule){
        int freeWorkersCount = getFreeWorkersCount(
                schedule.getPlannedDate(),
                scheduleRepo.getScheduleStartTime(schedule.getId()),
                scheduleRepo.getScheduleEndTime(schedule.getId()));
        if(schedule.getMaxWorkersCount() > freeWorkersCount)
            throw new AS_15_8_PPM_Exception("Ошибка планирования расписания. Количество свободных обработчиков уменьшилось. На данный момент их максимальное количество " + freeWorkersCount + " штук");

        arrangementRepo.findAllBySchedule(schedule.getId())
                .forEach(this::fillCheckUnits);
        schedule.setStatus(ScheduleStatus.PLANNED);
        scheduleRepo.save(schedule);
        log.info("Изменяем статусы всем мероприятиям из расписания с ИД: {}", schedule.getId());
        arrangementRepo.findAllBySchedule(schedule.getId()).forEach(
            arrangement -> arrangementStatusUploader.changeArrangementStatus(
                new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.SCHEDULE)
            )
        );
        log.info("Cтатусы всех мероприятий из расписания с ИД: {} сохранены, расписание запланировано", schedule.getId());
        return schedule;
    }

    public synchronized Schedule saveSchedule(List<Long> arrangementIds, String author, LocalDate plannedDate){
        return saveSchedule(arrangementIds, author, plannedDate, null);
    }

    public synchronized Schedule saveSchedule(List<Long> arrangementIds, String author, LocalDate plannedDate, Schedule schedule) {
        List<Long> availableIds;
        if (schedule == null){
            availableIds =
                    arrangementService.findAllAvailableArrangements()
                            .stream()
                            .mapToLong(Arrangement::getId)
                            .filter(arrangementIds::contains)
                            .boxed()
                            .collect(Collectors.toList());
        } else {
            availableIds = arrangementIds;
        }
        if (availableIds.size() == 0){
            throw new AS_15_8_PPM_Exception("Ошибка сохранения расписания. Список мероприятий не содержит незапланнированных мероприятий");
        }
        if(plannedDate==null || plannedDate.isBefore(LocalDate.now())){
            plannedDate = LocalDate.now();
        }
        log.info("Начало расчета расписания на дату: {}", plannedDate);
        Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits = arrangementService.getArrangementCheckUnits(availableIds, plannedDate);

        //Сначала исключим из расписания все периоды
        int maxWorkersCount;
        if(schedule != null) {
            clearSchedulePeriods(schedule);
            maxWorkersCount = schedule.getMaxWorkersCount();
        } else {
            maxWorkersCount = getMaxWorkersCount(arrangementCheckUnits.keySet(), plannedDate);
        }
        if(maxWorkersCount <= 0){
            throw new AS_15_8_PPM_Exception("Ошибка при создании расписания! В данный момент в системе не осталось свободных обработчиков!");
        }
        Schedule newSchedule = scheduleCreationService.create(arrangementCheckUnits, maxWorkersCount);
        log.info("Расчет расписания на дату {} завершен", plannedDate);
        if(schedule != null) {
            schedule.setSchedulePeriods(newSchedule.getSchedulePeriods());
            for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods())
                schedulePeriod.setSchedule(schedule);
        } else {
            schedule = newSchedule;
        }
        schedule.setAuthor(author);
        schedule.setPlannedDate(plannedDate);
        return scheduleRepo.save(schedule);
    }

    public int getFreeWorkersCount(LocalDate plannedDate, LocalTime startTime, LocalTime endTime){
        return Math.max(schedulerProperties.getTotalWorkersCount() - scheduleRepo.getBusyWorkersCount(plannedDate, startTime, endTime), 0);
    }

    private int getMaxWorkersCount(Set<Arrangement> arrangements, LocalDate plannedDate){
        LocalTime startTime = arrangements.stream()
                .map(Arrangement::getPlannedStartTime)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.now());
        LocalTime endTime = arrangements.stream()
                .map(Arrangement::getPlannedEndTime)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.now());
        return getFreeWorkersCount(plannedDate, startTime, endTime);
    }

    private void fillCheckUnits(Arrangement arrangement){
        List<SchedulePeriodArrangement> schedulePeriodArrangements = schedulePeriodArrangementRepo.findAllByArrangement(arrangement);
        SortedSet<ScheduleCheckUnit> scheduleCheckUnits = new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
        log.debug("Поиск scheduleCheckUnits по мероприятию: {}", arrangement.getId());
        scheduleCheckUnits.addAll(scheduleCheckUnitRepo.findAllByArrangement(arrangement));
        log.debug("Поиск scheduleCheckUnits по мероприятию: {} завершен", arrangement.getId());
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriodArrangements){
            if(!scheduleCheckUnits.isEmpty()){
                scheduleCheckUnits = fillSchedulePeriodArrangement(schedulePeriodArrangement, scheduleCheckUnits);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private SortedSet<ScheduleCheckUnit> fillSchedulePeriodArrangement(SchedulePeriodArrangement schedulePeriodArrangement,
                                                                       SortedSet<ScheduleCheckUnit> scheduleCheckUnits){
        ArrangementSchedulePeriodProcessing periodProcessing = scheduleCreationService.calculateArrangementSchedulePeriodProcessing(
                scheduleCheckUnits,
                schedulePeriodArrangement.getWorkersCount(),
                schedulePeriodArrangement.getArrangement().getAccessTool(),
                schedulePeriodArrangement.getSchedulePeriod().getStartTime(),
                schedulePeriodArrangement.getSchedulePeriod().getEndTime()
        );
        long currentExecutionNumber = 1;
        log.debug("Заполняется SPA {}", schedulePeriodArrangement.getId());
        SortedSet<ScheduleCheckUnit> periodCheckUnits = ((TreeSet)scheduleCheckUnits).headSet(periodProcessing.getLastCheckUnit(), true);
        for (ScheduleCheckUnit scheduleCheckUnit : periodCheckUnits){
            SchedulePeriodCheckUnit schedulePeriodCheckUnit = new SchedulePeriodCheckUnit();
            schedulePeriodCheckUnit.setSchedulePeriodArrangement(schedulePeriodArrangement);
            schedulePeriodCheckUnit.setCheckUnit(scheduleCheckUnit);
            schedulePeriodCheckUnit.setExecutionNumber(currentExecutionNumber++);
            schedulePeriodArrangement.getSchedulePeriodCheckUnits().add(schedulePeriodCheckUnit);
            schedulePeriodCheckUnitRepo.save(schedulePeriodCheckUnit);
            log.debug("Добавлен новый CheckUinit: {}", schedulePeriodCheckUnit.getExecutionNumber());
        }
        schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
        log.debug("SPA {} заполнено", schedulePeriodArrangement.getId());
        return ((TreeSet)scheduleCheckUnits).tailSet(periodProcessing.getLastCheckUnit(), false);
    }

    private void clearSchedulePeriods(Schedule schedule){
        schedule.getSchedulePeriods().forEach(schedulePeriodRepo::delete);
        schedule.getSchedulePeriods().clear();
    }
}