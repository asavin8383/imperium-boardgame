package service;

import checkUnits.CheckUnit;
import enums.AccessToolUnit;
import model.Arrangement;
import model.ArrangementProcessingPart;
import model.Schedule;
import model.SchedulePeriod;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleFactory {

    private static final int totalWorkersCount = 200;

    public static Schedule create(List<Arrangement> arrangements){
        Schedule schedule = createNewSchedule(arrangements);
        calculateWorkers(schedule);
        checkSchedule(schedule);
        return schedule;
    }

    private static Schedule createNewSchedule(List<Arrangement> arrangements){
        Schedule schedule = new Schedule();

        TreeSet<LocalTime> scheduleIntervals = new TreeSet<>();
        for(Arrangement arrangement : arrangements){
            scheduleIntervals.add(arrangement.getPlannedStartDate());
            scheduleIntervals.add(arrangement.getPlannedEndDate());
        }

        for(LocalTime startOfPeriod : scheduleIntervals){
            LocalTime endOfPeriod = scheduleIntervals.higher(startOfPeriod);
            if(endOfPeriod != null) {
                SchedulePeriod schedulePeriod = new SchedulePeriod(startOfPeriod, endOfPeriod);
                for(Arrangement arrangement : arrangements){
                    if(startOfPeriod.compareTo(arrangement.getPlannedStartDate()) >= 0 &&
                            endOfPeriod.compareTo(arrangement.getPlannedEndDate()) <= 0){

                        schedulePeriod.addArrangementProcessingPart(new ArrangementProcessingPart(arrangement));
                    }
                }
                schedule.getSchedulePeriods().add(schedulePeriod);
            }
        }
        schedule.getSchedulePeriods().sort(Comparator.comparing(SchedulePeriod::getStartTime));
        return schedule;
    }

    private static void calculateWorkers(Schedule schedule){
        Map<Arrangement, CheckUnit> nextCheckUnits = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(ArrangementProcessingPart processingPart : schedulePeriod.getArrangementProcessingParts()){
                nextCheckUnits.put(processingPart.getArrangement(), processingPart.getArrangement().getCheckUnits().first());
            }
        }

        for(int i = 0; i<schedule.getSchedulePeriods().size(); i++){
            SchedulePeriod schedulePeriod = schedule.getSchedulePeriods().get(i);
            Map<Arrangement, Double> arrangementDensities = calculateDensities(schedulePeriod, nextCheckUnits);
            double totalPeriodDensity = arrangementDensities.values().stream().collect(Collectors.summingDouble(Double::doubleValue));

            Map<Arrangement, CheckUnit> notFinishedArrangements = new HashMap<>();
            Iterator<ArrangementProcessingPart> arrProcessingIter = schedulePeriod.getArrangementProcessingParts().iterator();
            while(arrProcessingIter.hasNext()){
                ArrangementProcessingPart arrangementProcessingPart = arrProcessingIter.next();
                Arrangement arrangement = arrangementProcessingPart.getArrangement();
                CheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

                if(firstCheckUnit == null) {
                    arrProcessingIter.remove();
                    continue;
                }

                double percent = arrangementDensities.get(arrangement) / totalPeriodDensity;
                int workersCount = Long.valueOf(Math.round(totalWorkersCount * percent)).intValue();
                arrangementProcessingPart.setWorkersCount(workersCount);

                CheckUnit nextCheckUnit = arrangement.getCheckUnits().higher(
                        getLastCompletionCheckUnits(arrangement, firstCheckUnit, workersCount,schedulePeriod.getStartTime(),schedulePeriod.getEndTime()));
                nextCheckUnits.put(arrangement, nextCheckUnit);


                if(nextCheckUnit != null){
                    System.out.println(schedulePeriod.getStartTime() + " - "+schedulePeriod.getEndTime()+": "+arrangement.getName()+", осталось "+arrangement.getCheckUnits().tailSet(nextCheckUnit).size());
                    if(schedule.getSchedulePeriods().indexOf(schedulePeriod) < schedule.getSchedulePeriods().size()-1) {
                        SchedulePeriod nextPeriod = schedule.getSchedulePeriods().get(schedule.getSchedulePeriods().indexOf(schedulePeriod)+1);
                        if (!containsArrangement(nextPeriod.getArrangementProcessingParts(), arrangement)) {
                            nextPeriod.addArrangementProcessingPart(new ArrangementProcessingPart(arrangement));
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
                    processingTime += countArrangementProcessingTime(entry.getKey().getCheckUnits().tailSet(entry.getValue()), entry.getKey().getType());
                    newSchedulePeriod.addArrangementProcessingPart(new ArrangementProcessingPart(entry.getKey()));
                }

                newSchedulePeriod.setEndTime(schedulePeriod.getEndTime().plusSeconds(processingTime / totalWorkersCount + 1));
                schedule.getSchedulePeriods().add(newSchedulePeriod);
            }
        }
    }

    private static boolean checkSchedule(Schedule schedule){
        boolean isScheduleCorrect = true;

        Map<Arrangement, Long> arrangementLags = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(ArrangementProcessingPart arrangementProcessingPart : schedulePeriod.getArrangementProcessingParts()){

                if(arrangementProcessingPart.getArrangement().getPlannedEndDate().compareTo(schedulePeriod.getStartTime()) <= 0){
                    if(!arrangementLags.containsKey(arrangementProcessingPart.getArrangement()))
                        arrangementLags.put(arrangementProcessingPart.getArrangement(), 0L);


                    long arrangementLag = ChronoUnit.MINUTES.between(schedulePeriod.getStartTime(), schedulePeriod.getEndTime());
                    arrangementLags.put(arrangementProcessingPart.getArrangement(), arrangementLags.get(arrangementProcessingPart.getArrangement()) + arrangementLag);
                    isScheduleCorrect = false;
                }
            }
        }

        for(Map.Entry<Arrangement, Long> lagEntry : arrangementLags.entrySet()){
            System.out.println("Мероприятие " + lagEntry.getKey().getName() + " запаздывает на " + lagEntry.getValue() + " минут");
        }

        return isScheduleCorrect;
    }

    private static long countArrangementProcessingTime(Set<CheckUnit> checkUnits, AccessToolUnit arrangementType){
        long processingTime = 0;
        for(CheckUnit checkUnit : checkUnits){
            processingTime += CheckUnitProcessingTimeService.getProcessingTime(arrangementType, checkUnit.getType());
        }
        return processingTime;
    }

    private static CheckUnit getLastCompletionCheckUnits(Arrangement arrangement, CheckUnit startCheckUnit, int workersCount, LocalTime startTime, LocalTime endTime){
        SortedSet<CheckUnit> checkedSet = arrangement.getCheckUnits().tailSet(startCheckUnit);
        CheckUnit lastCompletionCheckUnit = checkedSet.first();
        long maxProcessingTime = ChronoUnit.SECONDS.between(startTime, endTime) * workersCount;
        long processingTime = 0;
        for(CheckUnit checkUnit : checkedSet){
            processingTime += CheckUnitProcessingTimeService.getProcessingTime(arrangement.getType(), checkUnit.getType());
            if(processingTime > maxProcessingTime){
                break;
            }
            lastCompletionCheckUnit = checkUnit;
        }
        return lastCompletionCheckUnit;
    }

    private static double calculateDensity(Set<CheckUnit> checkUnits, AccessToolUnit arrangementType, LocalTime startTime, LocalTime endTime){
        long processingTime = countArrangementProcessingTime(checkUnits, arrangementType);
        long arrangementPlannedDuration = ChronoUnit.SECONDS.between(startTime, endTime);
        return (double) processingTime / arrangementPlannedDuration;
    }

    private static Map<Arrangement, Double> calculateDensities(SchedulePeriod schedulePeriod, Map<Arrangement, CheckUnit> nextCheckUnits){
        Map<Arrangement, Double> arrangementDensities = new HashMap<>();
        for(ArrangementProcessingPart arrangementProcessingPart : schedulePeriod.getArrangementProcessingParts()){
            Arrangement arrangement = arrangementProcessingPart.getArrangement();
            CheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

            if(firstCheckUnit == null)
                continue;

            double density = calculateDensity(arrangement.getCheckUnits().tailSet(firstCheckUnit), arrangement.getType(),
                    schedulePeriod.getStartTime(), schedulePeriod.getEndTime());

            arrangementDensities.put(arrangement, density);
        }
        return arrangementDensities;
    }

    private static boolean containsArrangement(List<ArrangementProcessingPart> nextArrangementParts, Arrangement arrangement){
        for(ArrangementProcessingPart nextArrangementPart : nextArrangementParts){
            if(nextArrangementPart.getArrangement().equals(arrangement)){
                return true;
            }
        }
        return false;
    }
}
