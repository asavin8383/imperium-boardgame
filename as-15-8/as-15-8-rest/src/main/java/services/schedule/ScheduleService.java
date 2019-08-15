package services.schedule;

import enums.AccessToolParameters;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import model.catalog.AccessTool;
import model.erdi.CheckUnit;
import enums.AccessToolUnit;
import model.schedule.Schedule;
import model.schedule.SchedulePeriod;
import model.schedule.SchedulePeriodCheckUnit;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.GlobalParametersRepository;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final int totalWorkersCount;

    private final PlannedProcessingTimeService plannedProcessingTimeService;

    @Autowired
    public ScheduleService(PlannedProcessingTimeService plannedProcessingTimeService, GlobalParametersRepository globalParametersRepository){
        this.plannedProcessingTimeService = plannedProcessingTimeService;
        try {
            this.totalWorkersCount = globalParametersRepository
                    .findById(AccessToolParameters.TOTAL_WORKERS_COUNT)
                    .map(g -> Integer.parseInt(g.getValue()))
                    .orElseThrow(() -> new Exception("Параметр TOTAL_WORKERS_COUNT не задан"));
        } catch (Exception ex){
            throw new AS_15_8_Exception("Ошибка при получении количества ресурсов из параметров в БД", ex);
        }
    }

    public Schedule create(Map<Arrangement, TreeSet<CheckUnit>> arrangementCheckUnits){
        Schedule schedule = createNewSchedule(arrangementCheckUnits);
        calculateWorkers(schedule, arrangementCheckUnits);
        checkSchedule(schedule);
        return schedule;
    }

    private Schedule createNewSchedule(Map<Arrangement, TreeSet<CheckUnit>> arrangementCheckUnits){
        Schedule schedule = new Schedule();

        TreeSet<LocalTime> scheduleIntervals = new TreeSet<>();
        for(Arrangement arrangement : arrangementCheckUnits.keySet()){
            scheduleIntervals.add(arrangement.getStartDate().toLocalTime());
            scheduleIntervals.add(arrangement.getEndDate().toLocalTime());
        }

        for(LocalTime startOfPeriod : scheduleIntervals){
            LocalTime endOfPeriod = scheduleIntervals.higher(startOfPeriod);
            if(endOfPeriod != null) {
                SchedulePeriod schedulePeriod = new SchedulePeriod(startOfPeriod, endOfPeriod);
                for(Arrangement arrangement : arrangementCheckUnits.keySet()){
                    if(startOfPeriod.compareTo(arrangement.getStartDate().toLocalTime()) >= 0 &&
                            endOfPeriod.compareTo(arrangement.getEndDate().toLocalTime()) <= 0){

                        schedulePeriod.getSchedulePeriodCheckUnits().add(new SchedulePeriodCheckUnit(arrangement));
                    }
                }
                schedule.getSchedulePeriods().add(schedulePeriod);
            }
        }
        schedule.getSchedulePeriods().sort(Comparator.comparing(SchedulePeriod::getStartTime));
        return schedule;
    }

    private void calculateWorkers(Schedule schedule, Map<Arrangement, TreeSet<CheckUnit>> arrangementCheckUnits){
        Map<Arrangement, CheckUnit> nextCheckUnits = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(SchedulePeriodCheckUnit schedulePeriodCheckUnit : schedulePeriod.getSchedulePeriodCheckUnits()){
                nextCheckUnits.put(schedulePeriodCheckUnit.getArrangement(), arrangementCheckUnits.get(schedulePeriodCheckUnit.getArrangement()).first());
            }
        }

        for(int i = 0; i<schedule.getSchedulePeriods().size(); i++){
            SchedulePeriod schedulePeriod = schedule.getSchedulePeriods().get(i);
            Map<Arrangement, Double> arrangementDensities = calculateDensities(schedulePeriod, arrangementCheckUnits, nextCheckUnits);
            double totalPeriodDensity = arrangementDensities.values().stream().collect(Collectors.summingDouble(Double::doubleValue));

            Map<Arrangement, CheckUnit> notFinishedArrangements = new HashMap<>();
            Iterator<SchedulePeriodCheckUnit> schedulePeriodCheckUnitIterator = schedulePeriod.getSchedulePeriodCheckUnits().iterator();
            while(schedulePeriodCheckUnitIterator.hasNext()){
                SchedulePeriodCheckUnit schedulePeriodCheckUnit = schedulePeriodCheckUnitIterator.next();
                Arrangement arrangement = schedulePeriodCheckUnit.getArrangement();
                CheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

                if(firstCheckUnit == null) {
                    schedulePeriodCheckUnitIterator.remove();
                    continue;
                }

                double percent = arrangementDensities.get(arrangement) / totalPeriodDensity;
                int workersCount = Long.valueOf(Math.round(totalWorkersCount * percent)).intValue();
                schedulePeriodCheckUnit.setWorkersCount(workersCount);

                CheckUnit nextCheckUnit = arrangementCheckUnits.get(arrangement).higher(
                        getLastCompletionCheckUnits(
                                arrangementCheckUnits.get(arrangement).tailSet(firstCheckUnit),
                                workersCount,
                                arrangement.getAccessTool(),
                                schedulePeriod.getStartTime(),
                                schedulePeriod.getEndTime()));
                nextCheckUnits.put(arrangement, nextCheckUnit);

                if(nextCheckUnit != null){
                    if(schedule.getSchedulePeriods().indexOf(schedulePeriod) < schedule.getSchedulePeriods().size()-1) {
                        SchedulePeriod nextPeriod = schedule.getSchedulePeriods().get(schedule.getSchedulePeriods().indexOf(schedulePeriod)+1);
                        if (!containsArrangement(nextPeriod.getSchedulePeriodCheckUnits(), arrangement)) {
                            nextPeriod.getSchedulePeriodCheckUnits().add(new SchedulePeriodCheckUnit(arrangement));
                        }
                    } else {
                        notFinishedArrangements.put(arrangement, nextCheckUnit);
                    }
                }
            }

            if(notFinishedArrangements.size() > 0) {
                SchedulePeriod newSchedulePeriod = new SchedulePeriod(schedulePeriod.getEndTime(), schedulePeriod.getEndTime().plusSeconds(1));
                long processingTime = 0;
                for (Map.Entry<Arrangement, CheckUnit> entry : notFinishedArrangements.entrySet()) {
                    processingTime += countArrangementProcessingTime(arrangementCheckUnits.get(entry.getKey()).tailSet(entry.getValue()), entry.getKey().getAccessTool());
                    newSchedulePeriod.getSchedulePeriodCheckUnits().add(new SchedulePeriodCheckUnit(entry.getKey()));
                }

                newSchedulePeriod.setEndTime(schedulePeriod.getEndTime().plusSeconds(processingTime / totalWorkersCount + 1));
                schedule.getSchedulePeriods().add(newSchedulePeriod);
            }
        }
    }

    private boolean checkSchedule(Schedule schedule){
        boolean isScheduleCorrect = true;

        Map<Arrangement, Long> arrangementLags = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(SchedulePeriodCheckUnit schedulePeriodCheckUnit : schedulePeriod.getSchedulePeriodCheckUnits()){

                if(schedulePeriodCheckUnit.getArrangement().getEndDate().toLocalTime().compareTo(schedulePeriod.getStartTime()) <= 0){
                    if(!arrangementLags.containsKey(schedulePeriodCheckUnit.getArrangement()))
                        arrangementLags.put(schedulePeriodCheckUnit.getArrangement(), 0L);
                    long arrangementLag = arrangementLags.get(schedulePeriodCheckUnit.getArrangement()) +
                            ChronoUnit.MINUTES.between(schedulePeriod.getStartTime(), schedulePeriod.getEndTime());
                    arrangementLags.put(schedulePeriodCheckUnit.getArrangement(), arrangementLag);
                    isScheduleCorrect = false;
                }
            }
        }

        for(Map.Entry<Arrangement, Long> lagEntry : arrangementLags.entrySet()){
            System.out.println("Мероприятие " + lagEntry.getKey().getTitle() + " запаздывает на " + lagEntry.getValue() + " минут");
        }

        return isScheduleCorrect;
    }

    private long countArrangementProcessingTime(Set<CheckUnit> checkUnits, AccessTool accessTool){
        long processingTime = 0;
        for(CheckUnit checkUnit : checkUnits){
            processingTime += plannedProcessingTimeService.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
        }
        return processingTime;
    }

    private CheckUnit getLastCompletionCheckUnits(SortedSet<CheckUnit> checkedSet, int workersCount, AccessTool accessTool, LocalTime startTime, LocalTime endTime){
        CheckUnit lastCompletionCheckUnit = checkedSet.first();
        long maxProcessingTime = ChronoUnit.SECONDS.between(startTime, endTime) * workersCount;
        long processingTime = 0;
        for(CheckUnit checkUnit : checkedSet){
            processingTime += plannedProcessingTimeService.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
            if(processingTime > maxProcessingTime){
                break;
            }
            lastCompletionCheckUnit = checkUnit;
        }
        return lastCompletionCheckUnit;
    }

    private double calculateDensity(Set<CheckUnit> checkUnits, AccessTool accessTool, LocalTime startTime, LocalTime endTime){
        long processingTime = countArrangementProcessingTime(checkUnits, accessTool);
        long arrangementPlannedDuration = ChronoUnit.SECONDS.between(startTime, endTime);
        return processingTime / arrangementPlannedDuration;
    }

    private Map<Arrangement, Double> calculateDensities(SchedulePeriod schedulePeriod, Map<Arrangement, TreeSet<CheckUnit>> arrangementCheckUnits, Map<Arrangement, CheckUnit> nextCheckUnits){
        Map<Arrangement, Double> arrangementDensities = new HashMap<>();
        for(SchedulePeriodCheckUnit schedulePeriodCheckUnit : schedulePeriod.getSchedulePeriodCheckUnits()){
            Arrangement arrangement = schedulePeriodCheckUnit.getArrangement();
            CheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

            if(firstCheckUnit == null)
                continue;

            double density = calculateDensity(
                    arrangementCheckUnits.get(arrangement).tailSet(firstCheckUnit),
                    arrangement.getAccessTool(),
                    schedulePeriod.getStartTime(),
                    schedulePeriod.getEndTime());

            arrangementDensities.put(arrangement, density);
        }
        return arrangementDensities;
    }

    private static boolean containsArrangement(List<SchedulePeriodCheckUnit> schedulePeriodCheckUnits, Arrangement arrangement){
        for(SchedulePeriodCheckUnit schedulePeriodCheckUnit : schedulePeriodCheckUnits){
            if(schedulePeriodCheckUnit.getArrangement().equals(arrangement)){
                return true;
            }
        }
        return false;
    }

}
