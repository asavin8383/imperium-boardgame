package services.schedule;

import enums.AccessToolParameters;
import exceptions.AS_15_8_Exception;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.catalog.AccessTool;
import model.enums.ScheduleStatus;
import model.schedule.*;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;
import repositories.GlobalParametersRepository;
import repositories.ScheduleRepo;
import repositories.schedule.ScheduleCheckUnitRepo;
import repositories.schedule.SchedulePeriodArrangementRepo;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class ScheduleService {

    @Getter
    private final int totalWorkersCount;

    private final PlannedProcessingTimeService plannedProcessingTimeService;

    private final ScheduleRepo scheduleRepo;

    private final SchedulePeriodArrangementRepo schedulePeriodArrangementRepo;

    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;

    private final ArrangementRepo arrangementRepo;

    @Autowired
    public ScheduleService(PlannedProcessingTimeService plannedProcessingTimeService, GlobalParametersRepository globalParametersRepository, ScheduleRepo scheduleRepo, SchedulePeriodArrangementRepo schedulePeriodArrangementRepo, ScheduleCheckUnitRepo scheduleCheckUnitRepo, ArrangementRepo arrangementRepo){
        this.plannedProcessingTimeService = plannedProcessingTimeService;
        this.scheduleRepo = scheduleRepo;
        this.schedulePeriodArrangementRepo = schedulePeriodArrangementRepo;
        this.scheduleCheckUnitRepo = scheduleCheckUnitRepo;
        this.arrangementRepo = arrangementRepo;
        try {
            this.totalWorkersCount = globalParametersRepository
                    .findById(AccessToolParameters.TOTAL_WORKERS_COUNT)
                    .map(g -> Integer.parseInt(g.getValue()))
                    .orElseThrow(() -> new Exception("Параметр TOTAL_WORKERS_COUNT не задан"));
        } catch (Exception ex){
            throw new AS_15_8_Exception("Ошибка при получении количества ресурсов из параметров в БД", ex);
        }
    }

    public Schedule saveSchedule(Schedule schedule){
        return scheduleRepo.save(schedule);
    }

    public void deleteSchedule(Schedule schedule){
        scheduleRepo.delete(schedule);
    }

    public Schedule create(Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits){
        Schedule schedule = createNewSchedule(arrangementCheckUnits);
        calculateWorkers(schedule, arrangementCheckUnits);
        //checkSchedule(schedule);
        return schedule;
    }

    /**
     * Сохранение ресурсов каждого мероприятия по периодам расписания
     * Установка статуса PLANNED расписанию
     * @param schedule расписание для планирования
     * @return Запланированное расписание
     */
    @Transactional
    public Schedule planSchedule(Schedule schedule){
        arrangementRepo.findAllBySchedule(schedule.getId())
                .forEach(this::fillCheckUnits);
        schedule.setStatus(ScheduleStatus.PLANNED);
        return scheduleRepo.save(schedule);
    }

    private Schedule createNewSchedule(Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits){
        Schedule schedule = new Schedule();

        TreeSet<LocalTime> scheduleIntervals = new TreeSet<>();
        for(Arrangement arrangement : arrangementCheckUnits.keySet()){
            scheduleIntervals.add(arrangement.getPlannedStartTime());
            scheduleIntervals.add(arrangement.getPlannedEndTime());
        }

        for(LocalTime startOfPeriod : scheduleIntervals){
            LocalTime endOfPeriod = scheduleIntervals.higher(startOfPeriod);
            if(endOfPeriod != null) {
                SchedulePeriod schedulePeriod = new SchedulePeriod(schedule, startOfPeriod, endOfPeriod);
                for(Arrangement arrangement : arrangementCheckUnits.keySet()){
                    if(startOfPeriod.compareTo(arrangement.getPlannedStartTime()) >= 0 &&
                            endOfPeriod.compareTo(arrangement.getPlannedEndTime()) <= 0){
                        schedulePeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(schedulePeriod, arrangement));
                    }
                }
                schedule.getSchedulePeriods().add(schedulePeriod);
            }
        }
        //Collections.sort(schedule.getSchedulePeriods(), Comparator.comparing(SchedulePeriod::getStartTime));
        return schedule;
    }

    private void calculateWorkers(Schedule schedule, Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits){
        Map<Arrangement, ScheduleCheckUnit> nextCheckUnits = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
                nextCheckUnits.put(schedulePeriodArrangement.getArrangement(), arrangementCheckUnits.get(schedulePeriodArrangement.getArrangement()).first());
            }
        }

        for(int i = 0; i<schedule.getSchedulePeriods().size(); i++){
            Iterator<SchedulePeriod> schedulePeriodIterator = schedule.getSchedulePeriods().iterator();
            for(int j = 0; j < i; j++){
                if(schedulePeriodIterator.hasNext())
                    schedulePeriodIterator.next();
            }

            SchedulePeriod schedulePeriod = schedulePeriodIterator.next();
            Map<Arrangement, Double> arrangementDensities = calculateDensities(schedulePeriod, arrangementCheckUnits, nextCheckUnits);
            double totalPeriodDensity = arrangementDensities.values().stream().mapToDouble(Double::doubleValue).sum();

            Map<Arrangement, ScheduleCheckUnit> notFinishedArrangements = new HashMap<>();
            Iterator<SchedulePeriodArrangement> schedulePeriodArrangementsIterator = schedulePeriod.getSchedulePeriodArrangements().iterator();
            int busyWorkers = 0;
            while(schedulePeriodArrangementsIterator.hasNext()){
                SchedulePeriodArrangement schedulePeriodArrangement = schedulePeriodArrangementsIterator.next();
                Arrangement arrangement = schedulePeriodArrangement.getArrangement();
                ScheduleCheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

                if(firstCheckUnit == null) {
                    schedulePeriodArrangementsIterator.remove();
                    continue;
                }

                double percent = arrangementDensities.get(arrangement) / totalPeriodDensity;
                int workersCount;
                if(!schedulePeriodArrangementsIterator.hasNext()) {
                    workersCount = this.totalWorkersCount - busyWorkers;
                } else {
                    workersCount = Long.valueOf(Math.round(totalWorkersCount * percent)).intValue();
                    busyWorkers += workersCount;
                }
                schedulePeriodArrangement.setWorkersCount(workersCount);

                ScheduleCheckUnit nextCheckUnit = arrangementCheckUnits.get(arrangement).higher(
                        getLastCompletionCheckUnits(
                                arrangementCheckUnits.get(arrangement).tailSet(firstCheckUnit),
                                workersCount,
                                arrangement.getAccessTool(),
                                schedulePeriod.getStartTime(),
                                schedulePeriod.getEndTime()));
                nextCheckUnits.put(arrangement, nextCheckUnit);

                if(nextCheckUnit != null){
                    if(schedulePeriod != schedule.getSchedulePeriods().last()) {
                        SchedulePeriod nextPeriod = schedule.getSchedulePeriodsAsTreeSet().higher(schedulePeriod);
                        if (nextPeriod!= null && !containsArrangement(nextPeriod.getSchedulePeriodArrangements(), arrangement)) {
                            nextPeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(nextPeriod, arrangement));
                        }
                    } else {
                        notFinishedArrangements.put(arrangement, nextCheckUnit);
                    }
                }
            }

            if(notFinishedArrangements.size() > 0) {
                SchedulePeriod newSchedulePeriod = new SchedulePeriod(schedule, schedulePeriod.getEndTime(), schedulePeriod.getEndTime().plusSeconds(1));
                long processingTime = 0;
                for (Map.Entry<Arrangement, ScheduleCheckUnit> entry : notFinishedArrangements.entrySet()) {
                    processingTime += countArrangementProcessingTime(arrangementCheckUnits.get(entry.getKey()).tailSet(entry.getValue()), entry.getKey().getAccessTool());
                    newSchedulePeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(newSchedulePeriod, entry.getKey()));
                }

                newSchedulePeriod.setEndTime(schedulePeriod.getEndTime().plusSeconds(processingTime / totalWorkersCount + 1));
                schedule.getSchedulePeriods().add(newSchedulePeriod);
            }
        }
    }

    /*private boolean checkSchedule(Schedule schedule){
        boolean isScheduleCorrect = true;

        Map<Arrangement, Long> arrangementLags = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){

                if(schedulePeriodArrangement.getArrangement().getPlannedEndTime().compareTo(schedulePeriod.getStartTime()) <= 0){
                    if(!arrangementLags.containsKey(schedulePeriodArrangement.getArrangement()))
                        arrangementLags.put(schedulePeriodArrangement.getArrangement(), 0L);
                    long arrangementLag = arrangementLags.get(schedulePeriodArrangement.getArrangement()) +
                            ChronoUnit.MINUTES.between(schedulePeriod.getStartTime(), schedulePeriod.getEndTime());
                    arrangementLags.put(schedulePeriodArrangement.getArrangement(), arrangementLag);
                    isScheduleCorrect = false;
                }
            }
        }

        for(Map.Entry<Arrangement, Long> lagEntry : arrangementLags.entrySet()){
            System.out.println("Мероприятие " + lagEntry.getKey().getTitle() + " запаздывает на " + lagEntry.getValue() + " минут");
        }

        return isScheduleCorrect;
    }*/

    private long countArrangementProcessingTime(Set<ScheduleCheckUnit> checkUnits, AccessTool accessTool){
        long processingTime = 0;
        for(ScheduleCheckUnit checkUnit : checkUnits){
            processingTime += plannedProcessingTimeService.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
        }
        return processingTime;
    }

    private ScheduleCheckUnit getLastCompletionCheckUnits(SortedSet<ScheduleCheckUnit> checkedSet, int workersCount, AccessTool accessTool, LocalTime startTime, LocalTime endTime){
        ScheduleCheckUnit lastCompletionCheckUnit = checkedSet.first();
        long maxProcessingTime = ChronoUnit.SECONDS.between(startTime, endTime) * workersCount;
        long processingTime = 0;
        for(ScheduleCheckUnit checkUnit : checkedSet){
            processingTime += plannedProcessingTimeService.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
            if(processingTime > maxProcessingTime){
                break;
            }
            lastCompletionCheckUnit = checkUnit;
        }
        return lastCompletionCheckUnit;
    }

    private double calculateDensity(Set<ScheduleCheckUnit> checkUnits, Arrangement arrangement, LocalTime startTime, LocalTime endTime){
        long processingTime = countArrangementProcessingTime(checkUnits, arrangement.getAccessTool());
        long arrangementPlannedDuration = ChronoUnit.SECONDS.between(startTime, endTime);
        double workersCoeff = 1;
        if(arrangement.getMaxWorkersCount() != null)
            workersCoeff = (double) arrangement.getMaxWorkersCount() / this.totalWorkersCount;
        return (double) processingTime / arrangementPlannedDuration * workersCoeff;
    }

    private Map<Arrangement, Double> calculateDensities(SchedulePeriod schedulePeriod, Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits, Map<Arrangement, ScheduleCheckUnit> nextCheckUnits){
        Map<Arrangement, Double> arrangementDensities = new HashMap<>();
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
            Arrangement arrangement = schedulePeriodArrangement.getArrangement();
            ScheduleCheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

            if(firstCheckUnit == null)
                continue;

            double density = calculateDensity(
                    arrangementCheckUnits.get(arrangement).tailSet(firstCheckUnit),
                    arrangement,
                    schedulePeriod.getStartTime(),
                    schedulePeriod.getEndTime());

            arrangementDensities.put(arrangement, density);
        }
        return arrangementDensities;
    }

    private static boolean containsArrangement(Set<SchedulePeriodArrangement> schedulePeriodArrangements, Arrangement arrangement){
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriodArrangements){
            if(schedulePeriodArrangement.getArrangement().equals(arrangement)){
                return true;
            }
        }
        return false;
    }

    private void fillCheckUnits(Arrangement arrangement){
        List<SchedulePeriodArrangement> schedulePeriodArrangements = schedulePeriodArrangementRepo.findAllByArrangement(arrangement);
        SortedSet<ScheduleCheckUnit> arrangementResults = new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
        log.debug("Поиск arrangementResults по мероприятию: {}", arrangement.getId());
        arrangementResults.addAll(scheduleCheckUnitRepo.findAllByArrangement(arrangement));
        log.debug("Поиск arrangementResults по мероприятию: {} завершен", arrangement.getId());
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriodArrangements){
            if(!arrangementResults.isEmpty()){
                arrangementResults = fillSchedulePeriodArrangement(schedulePeriodArrangement, arrangementResults);
            }
        }
    }


    private SortedSet<ScheduleCheckUnit> fillSchedulePeriodArrangement(SchedulePeriodArrangement schedulePeriodArrangement, SortedSet<ScheduleCheckUnit> scheduleCheckUnits){
        long totalTime = countSchedulePeriodArrangementTotalTime(schedulePeriodArrangement);
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
            //schedulePeriodCheckUnitRepo.save(schedulePeriodCheckUnit);
            long plannedProcessingTime = plannedProcessingTimeService.getProcessingTime(schedulePeriodArrangement.getArrangement().getAccessTool(), scheduleCheckUnit.getCheckUnitType());
            if (plannedProcessingTime <= 0){
                AS_15_8_Exception.logAndThrow(log, String
                        .format("Некорректное время обработки для ПАСД %s типа ресурса %s - значение: %d", schedulePeriodArrangement.getArrangement().getAccessTool().getName(), scheduleCheckUnit.getCheckUnitType(), plannedProcessingTime));
            }
            totalTime -= plannedProcessingTime;
            log.debug("Добавлен новый CheckUinit: {}", schedulePeriodCheckUnit.getExecutionNumber());
        }
        schedulePeriodArrangementRepo.save(schedulePeriodArrangement);
        log.debug("SPA {} заполнено", schedulePeriodArrangement.getId());
        return new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
    }

    private long countSchedulePeriodArrangementTotalTime(SchedulePeriodArrangement schedulePeriodArrangement){
        SchedulePeriod schedulePeriod = schedulePeriodArrangement.getSchedulePeriod();
        long durationMS = Duration.between(schedulePeriod.getStartTime(), schedulePeriod.getEndTime()).toMillis();
        return durationMS * schedulePeriodArrangement.getWorkersCount();
    }
}
