package services.schedule;

import enums.AccessToolParameters;
import exceptions.AS_15_8_Exception;
import model.catalog.AccessTool;
import model.result.ArrangementResult;
import model.schedule.Schedule;
import model.schedule.SchedulePeriod;
import model.schedule.SchedulePeriodArrangement;
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

    public Schedule create(Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits){
        Schedule schedule = createNewSchedule(arrangementCheckUnits);
        calculateWorkers(schedule, arrangementCheckUnits);
        //checkSchedule(schedule);
        return schedule;
    }

    private Schedule createNewSchedule(Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits){
        Schedule schedule = new Schedule();

        TreeSet<LocalTime> scheduleIntervals = new TreeSet<>();
        for(Arrangement arrangement : arrangementCheckUnits.keySet()){
            scheduleIntervals.add(arrangement.getPlannedStartTime());
            scheduleIntervals.add(arrangement.getPlannedEndTime());
        }

        for(LocalTime startOfPeriod : scheduleIntervals){
            LocalTime endOfPeriod = scheduleIntervals.higher(startOfPeriod);
            if(endOfPeriod != null) {
                SchedulePeriod schedulePeriod = new SchedulePeriod(startOfPeriod, endOfPeriod);
                for(Arrangement arrangement : arrangementCheckUnits.keySet()){
                    if(startOfPeriod.compareTo(arrangement.getPlannedStartTime()) >= 0 &&
                            endOfPeriod.compareTo(arrangement.getPlannedEndTime()) <= 0){

                        schedulePeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(arrangement));
                    }
                }
                schedule.getSchedulePeriods().add(schedulePeriod);
            }
        }
        schedule.getSchedulePeriods().sort(Comparator.comparing(SchedulePeriod::getStartTime));
        return schedule;
    }

    private void calculateWorkers(Schedule schedule, Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits){
        Map<Arrangement, ArrangementResult> nextCheckUnits = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
                nextCheckUnits.put(schedulePeriodArrangement.getArrangement(), arrangementCheckUnits.get(schedulePeriodArrangement.getArrangement()).first());
            }
        }

        for(int i = 0; i<schedule.getSchedulePeriods().size(); i++){
            SchedulePeriod schedulePeriod = schedule.getSchedulePeriods().get(i);
            Map<Arrangement, Double> arrangementDensities = calculateDensities(schedulePeriod, arrangementCheckUnits, nextCheckUnits);
            double totalPeriodDensity = arrangementDensities.values().stream().collect(Collectors.summingDouble(Double::doubleValue));

            Map<Arrangement, ArrangementResult> notFinishedArrangements = new HashMap<>();
            Iterator<SchedulePeriodArrangement> schedulePeriodArrangementsIterator = schedulePeriod.getSchedulePeriodArrangements().iterator();
            int busyWorkers = 0;
            while(schedulePeriodArrangementsIterator.hasNext()){
                SchedulePeriodArrangement schedulePeriodArrangement = schedulePeriodArrangementsIterator.next();
                Arrangement arrangement = schedulePeriodArrangement.getArrangement();
                ArrangementResult firstCheckUnit = nextCheckUnits.get(arrangement);

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

                ArrangementResult nextCheckUnit = arrangementCheckUnits.get(arrangement).higher(
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
                        if (!containsArrangement(nextPeriod.getSchedulePeriodArrangements(), arrangement)) {
                            nextPeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(arrangement));
                        }
                    } else {
                        notFinishedArrangements.put(arrangement, nextCheckUnit);
                    }
                }
            }

            if(notFinishedArrangements.size() > 0) {
                SchedulePeriod newSchedulePeriod = new SchedulePeriod(schedulePeriod.getEndTime(), schedulePeriod.getEndTime().plusSeconds(1));
                long processingTime = 0;
                for (Map.Entry<Arrangement, ArrangementResult> entry : notFinishedArrangements.entrySet()) {
                    processingTime += countArrangementProcessingTime(arrangementCheckUnits.get(entry.getKey()).tailSet(entry.getValue()), entry.getKey().getAccessTool());
                    newSchedulePeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(entry.getKey()));
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
    }

    private long countArrangementProcessingTime(Set<ArrangementResult> checkUnits, AccessTool accessTool){
        long processingTime = 0;
        for(ArrangementResult checkUnit : checkUnits){
            processingTime += plannedProcessingTimeService.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
        }
        return processingTime;
    }

    private ArrangementResult getLastCompletionCheckUnits(SortedSet<ArrangementResult> checkedSet, int workersCount, AccessTool accessTool, LocalTime startTime, LocalTime endTime){
        ArrangementResult lastCompletionCheckUnit = checkedSet.first();
        long maxProcessingTime = ChronoUnit.SECONDS.between(startTime, endTime) * workersCount;
        long processingTime = 0;
        for(ArrangementResult checkUnit : checkedSet){
            processingTime += plannedProcessingTimeService.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
            if(processingTime > maxProcessingTime){
                break;
            }
            lastCompletionCheckUnit = checkUnit;
        }
        return lastCompletionCheckUnit;
    }

    private double calculateDensity(Set<ArrangementResult> checkUnits, Arrangement arrangement, LocalTime startTime, LocalTime endTime){
        long processingTime = countArrangementProcessingTime(checkUnits, arrangement.getAccessTool());
        long arrangementPlannedDuration = ChronoUnit.SECONDS.between(startTime, endTime);
        double workersCoeff = 1;
        if(arrangement.getMaxWorkersCount() != null)
            workersCoeff = (double) arrangement.getMaxWorkersCount() / this.totalWorkersCount;
        return (double) processingTime / arrangementPlannedDuration * workersCoeff;
    }

    private Map<Arrangement, Double> calculateDensities(SchedulePeriod schedulePeriod, Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits, Map<Arrangement, ArrangementResult> nextCheckUnits){
        Map<Arrangement, Double> arrangementDensities = new HashMap<>();
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
            Arrangement arrangement = schedulePeriodArrangement.getArrangement();
            ArrangementResult firstCheckUnit = nextCheckUnits.get(arrangement);

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

    private static boolean containsArrangement(List<SchedulePeriodArrangement> schedulePeriodArrangements, Arrangement arrangement){
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriodArrangements){
            if(schedulePeriodArrangement.getArrangement().equals(arrangement)){
                return true;
            }
        }
        return false;
    }

}
