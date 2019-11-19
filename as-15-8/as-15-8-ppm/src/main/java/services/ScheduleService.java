package services;

import arrangement.ArrangementStatusNotification;
import common.SchedulerException;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScheduleService {

    private final ScheduleCreationService scheduleCreationService;
    private final ArrangementService arrangementService;
    private final SchedulerProperties schedulerProperties;
    private final ScheduleRepo scheduleRepo;
    private final SchedulePeriodArrangementRepo schedulePeriodArrangementRepo;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final SchedulePeriodCheckUnitRepo schedulePeriodCheckUnitRepo;
    private final ArrangementRepo arrangementRepo;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final SchedulePeriodRepo schedulePeriodRepo;

    public void deleteSchedule(Schedule schedule){
        scheduleRepo.delete(schedule);
    }

    /**
     * Сохранение ресурсов каждого мероприятия по периодам расписания
     * Установка статуса PLANNED расписанию
     * @return Запланированное расписание
     */
    @Transactional
    public Schedule planSchedule(Schedule schedule){
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


    private SortedSet<ScheduleCheckUnit> fillSchedulePeriodArrangement(SchedulePeriodArrangement schedulePeriodArrangement,
                                                                       SortedSet<ScheduleCheckUnit> scheduleCheckUnits){
        long totalTime = scheduleCreationService.calculateArrangementSchedulePeriodProcessing(
                scheduleCheckUnits,
                schedulePeriodArrangement.getWorkersCount(),
                schedulePeriodArrangement.getArrangement().getAccessTool(),
                schedulePeriodArrangement.getSchedulePeriod().getStartTime(),
                schedulePeriodArrangement.getSchedulePeriod().getEndTime()
        ).getTime();
        long i = 1;
        log.debug("Заполняется SPA {}", schedulePeriodArrangement.getId());
        for (ScheduleCheckUnit scheduleCheckUnit : scheduleCheckUnits){
            if (totalTime <= 0){
                scheduleCheckUnits = scheduleCheckUnits.tailSet(scheduleCheckUnit);
                return scheduleCheckUnits.tailSet(scheduleCheckUnit);
            }
            SchedulePeriodCheckUnit schedulePeriodCheckUnit = new SchedulePeriodCheckUnit();
            schedulePeriodCheckUnit.setSchedulePeriodArrangement(schedulePeriodArrangement);
            schedulePeriodCheckUnit.setCheckUnit(scheduleCheckUnit);
            schedulePeriodCheckUnit.setExecutionNumber(i++);
            schedulePeriodArrangement.getSchedulePeriodCheckUnits().add(schedulePeriodCheckUnit);
            schedulePeriodCheckUnitRepo.save(schedulePeriodCheckUnit);
            long plannedProcessingTime = schedulerProperties.getProcessingTime(
                    schedulePeriodArrangement.getArrangement().getAccessTool(),
                    scheduleCheckUnit.getCheckUnitType());
            if (plannedProcessingTime <= 0){
                throw new SchedulerException(String
                        .format("Некорректное время обработки для ПАСД %s типа ресурса %s - значение: %d",
                                schedulePeriodArrangement.getArrangement().getAccessTool(),
                                scheduleCheckUnit.getCheckUnitType(),
                                plannedProcessingTime));
            }
            totalTime -= plannedProcessingTime;
            log.debug("Добавлен новый CheckUinit: {}", schedulePeriodCheckUnit.getExecutionNumber());
        }
        schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
        log.debug("SPA {} заполнено", schedulePeriodArrangement.getId());
        return new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
    }

    public synchronized Schedule saveSchedule(List<Long> arrangementIds, String author, LocalDate plannedDate, Schedule schedule){
        //Сначала исключим из расписания все периоды
        if(schedule != null)
            clearSchedulePeriods(schedule);

        List<Long> availableIds =
                arrangementService.findAllAvailableArrangements()
                        .stream()
                        .mapToLong(Arrangement::getId)
                        .filter(arrangementIds::contains)
                        .boxed()
                        .collect(Collectors.toList());
        if (availableIds.size() == 0){
            throw AS_15_8_PPM_Exception.logAndGet(log,"Ошибка сохранения расписания. Список мероприятий не содержит незапланнированных мероприятий");
        }
        if(plannedDate==null || plannedDate.isBefore(LocalDate.now())){
            plannedDate = LocalDate.now();
        }
        log.info("Начало расчета расписания на дату: {}", plannedDate);
        Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits = arrangementService.getArrangementCheckUnits(availableIds, plannedDate);
        Schedule newSchedule = scheduleCreationService.create(arrangementCheckUnits);
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

    private void clearSchedulePeriods(Schedule schedule){
        schedule.getSchedulePeriods().forEach(schedulePeriodRepo::delete);
        schedule.getSchedulePeriods().clear();
    }
}