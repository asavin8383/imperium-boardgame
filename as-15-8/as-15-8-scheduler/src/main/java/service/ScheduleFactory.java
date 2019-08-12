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

    private static final int totalWorkersCount = 2000;

    public static Schedule create(List<Arrangement> arrangements){
        Schedule schedule = createNewSchedule(arrangements);
        calculateWorkers(schedule);
        checkSchedule(schedule, arrangements);
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
        return schedule;
    }

    private static void calculateWorkers(Schedule schedule){
        Map<Arrangement, CheckUnit> nextCheckUnits = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(ArrangementProcessingPart processingPart : schedulePeriod.getArrangementProcessingParts()){
                nextCheckUnits.put(processingPart.getArrangement(), processingPart.getArrangement().getCheckUnits().first());
            }
        }

        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            Map<Arrangement, Double> arrangementDensities = calculateDensities(schedulePeriod, nextCheckUnits);
            double totalPeriodDensity = arrangementDensities.values().stream().collect(Collectors.summingDouble(Double::doubleValue));

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

                CheckUnit lastCheckUnit = getLastCompletionCheckUnits(arrangement, firstCheckUnit, workersCount,
                        schedulePeriod.getStartTime(),schedulePeriod.getEndTime());
                nextCheckUnits.put(arrangement, arrangement.getCheckUnits().higher(lastCheckUnit));
            }
        }
    }

    private static boolean checkSchedule(Schedule schedule, List<Arrangement> arrangements){
        boolean isScheduleCorrect = true;

        Map<Arrangement, Long> arrangementScheduleTimes = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(ArrangementProcessingPart arrangementProcessingPart : schedulePeriod.getArrangementProcessingParts()){
                if(!arrangementScheduleTimes.containsKey(arrangementProcessingPart.getArrangement()))
                    arrangementScheduleTimes.put(arrangementProcessingPart.getArrangement(), 0L);

                long arrangementPeriodTime = arrangementScheduleTimes.get(arrangementProcessingPart.getArrangement()) +
                        (arrangementProcessingPart.getWorkersCount() *
                        ChronoUnit.SECONDS.between(schedulePeriod.getStartTime(), schedulePeriod.getEndTime()));
                arrangementScheduleTimes.put(arrangementProcessingPart.getArrangement(), arrangementPeriodTime);
            }
        }

        for(Arrangement arrangement : arrangements){
            long totalTime = countArrangementProcessingTime(arrangement.getCheckUnits(), arrangement.getType());
            if(totalTime > arrangementScheduleTimes.get(arrangement)){
                System.out.println("Мероприятие "+arrangement.getName()+ " не вписалось по времени на " +
                        ((double)(totalTime - arrangementScheduleTimes.get(arrangement)) / 60 / totalWorkersCount) + " минут");
                isScheduleCorrect = false;
            }
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
        return processingTime / arrangementPlannedDuration;
    }

    private static Map<Arrangement, Double> calculateDensities(SchedulePeriod schedulePeriod, Map<Arrangement, CheckUnit> nextCheckUnits){
        Map<Arrangement, Double> arrangementDensities = new HashMap<>();
        for(ArrangementProcessingPart arrangementProcessingPart : schedulePeriod.getArrangementProcessingParts()){
            Arrangement arrangement = arrangementProcessingPart.getArrangement();
            CheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

            if(firstCheckUnit == null)
                continue;

            double density = calculateDensity(arrangement.getCheckUnits().tailSet(firstCheckUnit), arrangement.getType(),
                    schedulePeriod.getStartTime(), arrangement.getPlannedEndDate());

            arrangementDensities.put(arrangement, density);
        }
        return arrangementDensities;
    }
}
