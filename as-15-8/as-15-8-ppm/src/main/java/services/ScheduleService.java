package services;

import arrangement.ArrangementStatusNotification;
import common.SchedulerProperties;
import enums.ArrangementEvents;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.ScheduleCheckUnitStatus;
import model.enums.SchedulePeriodState;
import model.enums.ScheduleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.*;
import restapi.ArrangementStatusUploader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Transactional
    public void deleteSchedule(Schedule schedule){
        List<Arrangement> arrangements = arrangementRepo.findAllBySchedule(schedule.getId());
        log.info("Удаляем расписание с ИД: {}", schedule.getId());
        scheduleRepo.delete(schedule);
        arrangements.forEach(arrangement -> {
                arrangementStatusUploader.changeArrangementStatus(
                    new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.SCHEDULE_ROLLBACK));
                log.info("Изменяем чек-юниты мероприятия с ИД: {} и статусом SCHEDULED", arrangement.getId());
                scheduleCheckUnitRepo.changeStatus(arrangement, ScheduleCheckUnitStatus.SCHEDULED, ScheduleCheckUnitStatus.NEW);
                log.info("Статусы чек-юнитов мероприятия с ИД: {} изменены на NEW", arrangement.getId());
            }
        );
    }

    /**
     * Сохранение ресурсов каждого мероприятия по периодам расписания
     * Установка статуса PLANNED расписанию
     * @return Запланированное расписание
     */
    @Transactional
    public Schedule planSchedule(Schedule schedule){
        if(schedule.getMaxWorkersCount() == 0)
            throw new AS_15_8_PPM_Exception("Ошибка планирования расписания. Выбрано 0 обработчиков");
        int freeWorkersCount = getFreeWorkersCount(
                schedule.getPlannedDate(),
                scheduleRepo.getScheduleStartTime(schedule.getId()),
                scheduleRepo.getScheduleEndTime(schedule.getId()));
        if(schedule.getMaxWorkersCount() > freeWorkersCount)
            throw new AS_15_8_PPM_Exception("Ошибка планирования расписания. Количество свободных обработчиков уменьшилось. На данный момент их максимальное количество " + freeWorkersCount + " штук");

        fillSchedule(schedule);

        schedule.getSchedulePeriods().forEach(schedulePeriod -> schedulePeriod.setSchedulePeriodState(SchedulePeriodState.PLANNED));
        schedule.setStatus(ScheduleStatus.PLANNED);
        scheduleRepo.save(schedule);
        log.info("Изменяем статусы всем мероприятиям из расписания с ИД: {}", schedule.getId());
        arrangementRepo.findAllBySchedule(schedule.getId()).forEach(
            arrangement -> {
                    arrangementStatusUploader.changeArrangementStatus(
                        new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.SCHEDULE));
                    log.info("Изменяем чек-юниты мероприятия с ИД: {} и статусом NEW", arrangement.getId());
                    scheduleCheckUnitRepo.changeStatus(arrangement, ScheduleCheckUnitStatus.NEW, ScheduleCheckUnitStatus.SCHEDULED);
                    log.info("Статусы чек-юнитов мероприятия с ИД: {} изменены на SCHEDULED", arrangement.getId());
            }
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
            if(schedule.getMaxWorkersCount() == 0)
                throw new AS_15_8_PPM_Exception("Ошибка расчета расписания. Выбрано 0 обработчиков");
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

    public void checkAndCloseSchedule(Schedule schedule){
        boolean needToClose = arrangementRepo.findAllBySchedule(schedule.getId())
                .stream()
                .allMatch(Arrangement::isClosed);
        if(needToClose){
            schedule.setStatus(ScheduleStatus.FINISHED);
            scheduleRepo.save(schedule);
            log.info("Статус расписания с ИД: {} сменился на 'ЗАКРЫТО'", schedule.getId());
        }
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

//    private long fillCheckUnits(Arrangement arrangement, long startExecutionNumber){
//        List<SchedulePeriodArrangement> schedulePeriodArrangements = schedulePeriodArrangementRepo.findAllByArrangement(arrangement);
//        SortedSet<ScheduleCheckUnit> scheduleCheckUnits = new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
//        log.debug("Поиск scheduleCheckUnits по мероприятию: {}", arrangement.getId());
//        scheduleCheckUnits.addAll(scheduleCheckUnitRepo.findAllByArrangement(arrangement));
//        log.debug("Поиск scheduleCheckUnits по мероприятию: {} завершен", arrangement.getId());
//
//        long maxArrWorkersCount = 0;
//        long curSize = scheduleCheckUnits.size();
//        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriodArrangements){
//            if(!scheduleCheckUnits.isEmpty()){
//                scheduleCheckUnits = fillSchedulePeriodArrangement(schedulePeriodArrangement, scheduleCheckUnits, startExecutionNumber);
//            }
//            maxArrWorkersCount = Math.max(maxArrWorkersCount, schedulePeriodArrangement.getWorkersCount());
//        }
//        return Math.min(curSize, maxArrWorkersCount);
//    }

    private void clearSchedulePeriods(Schedule schedule){
        schedule.getSchedulePeriods().forEach(schedulePeriodRepo::delete);
        schedule.getSchedulePeriods().clear();
    }

    private void fillSchedule(Schedule schedule){

        Map<Arrangement, NavigableSet<ScheduleCheckUnit>> arrangementCheckUnits = new HashMap<>();
        List<Arrangement> arrangements = arrangementRepo.findAllBySchedule(schedule.getId());
        for(Arrangement arrangement : arrangements) {
            arrangementCheckUnits.put(
                    arrangement,
                    new TreeSet<>(scheduleCheckUnitRepo.findAllByArrangement(arrangement))
            );
        }

        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()) {
            NavigableSet<Long> freeExecutionNumbers = getFreeExecutionNumbers(schedule.getId(), schedule.getPlannedDate());
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()) {
                TreeSet<Long> curFreeExecutionNumbers = freeExecutionNumbers
                        .stream()
                        .limit(schedulePeriodArrangement.getWorkersCount())
                        .collect(Collectors.toCollection(TreeSet::new));
                freeExecutionNumbers = freeExecutionNumbers.tailSet(curFreeExecutionNumbers.last(), false);

                NavigableSet<ScheduleCheckUnit> tailCheckUnits = fillSchedulePeriodArrangement(
                        schedulePeriodArrangement,
                        arrangementCheckUnits.get(schedulePeriodArrangement.getArrangement()),
                        curFreeExecutionNumbers
                );
                arrangementCheckUnits.put(schedulePeriodArrangement.getArrangement(), tailCheckUnits);
            }
        }
    }

    private TreeSet<Long> getFreeExecutionNumbers(Long scheduleId, LocalDate plannedDate){
        List<Long> busyExecutionNumbers = schedulePeriodCheckUnitRepo.getBusyExecutionNumbers(
                plannedDate,
                scheduleRepo.getScheduleStartTime(scheduleId),
                scheduleRepo.getScheduleEndTime(scheduleId));
        return Stream.iterate(0L, num -> num + 1L)
                .limit(schedulerProperties.getTotalWorkersCount())
                .filter(num -> !busyExecutionNumbers.contains(num))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private NavigableSet<ScheduleCheckUnit> fillSchedulePeriodArrangement(SchedulePeriodArrangement schedulePeriodArrangement,
                                                                       NavigableSet<ScheduleCheckUnit> scheduleCheckUnits,
                                                                       TreeSet<Long> freeExecutionNumbers){
        if(freeExecutionNumbers ==null || freeExecutionNumbers.size() == 0)
            throw new AS_15_8_PPM_Exception("Ошибка при заполнении проверок периода. Не найдено свободных обработчиков");

        ArrangementSchedulePeriodProcessing periodProcessing = scheduleCreationService.calculateArrangementSchedulePeriodProcessing(
                scheduleCheckUnits,
                schedulePeriodArrangement.getWorkersCount(),
                schedulePeriodArrangement.getArrangement().getAccessTool(),
                schedulePeriodArrangement.getSchedulePeriod().getStartTime(),
                schedulePeriodArrangement.getSchedulePeriod().getEndTime()
        );
        Iterator<Long> freeExecutionNumbersIterator = freeExecutionNumbers.iterator();
        log.debug("Заполняется SPA {}", schedulePeriodArrangement.getId());
        SortedSet<ScheduleCheckUnit> periodCheckUnits = scheduleCheckUnits.headSet(periodProcessing.getLastCheckUnit(), true);
        for (ScheduleCheckUnit scheduleCheckUnit : periodCheckUnits){

            if(!freeExecutionNumbersIterator.hasNext())
                freeExecutionNumbersIterator = freeExecutionNumbers.iterator();

            SchedulePeriodCheckUnit schedulePeriodCheckUnit = new SchedulePeriodCheckUnit();
            schedulePeriodCheckUnit.setSchedulePeriodArrangement(schedulePeriodArrangement);
            schedulePeriodCheckUnit.setCheckUnit(scheduleCheckUnit);
            schedulePeriodCheckUnit.setExecutionNumber(freeExecutionNumbersIterator.next());
            schedulePeriodArrangement.getSchedulePeriodCheckUnits().add(schedulePeriodCheckUnit);
            schedulePeriodCheckUnitRepo.save(schedulePeriodCheckUnit);
            log.debug("Добавлен новый CheckUnit: {}", schedulePeriodCheckUnit.getExecutionNumber());
        }
        schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
        log.debug("SPA {} заполнено", schedulePeriodArrangement.getId());
        return scheduleCheckUnits.tailSet(periodProcessing.getLastCheckUnit(), false);
    }
}