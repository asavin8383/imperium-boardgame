package services;

import common.SchedulerException;
import common.SchedulerProperties;
import exceptions.AS_15_8_PPM_Exception;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScheduleCreationService {

    @Getter
    private int totalWorkersCount;

    private final SchedulerProperties schedulerProperties;

    @PostConstruct
    public void init(){
        try {
            this.totalWorkersCount = Optional.ofNullable(
                    schedulerProperties.getTotalWorkersCount())
                    .orElseThrow(() -> new Exception("Параметр TOTAL_WORKERS_COUNT не задан"));
        } catch (Exception ex){
            throw new SchedulerException("Ошибка при получении количества ресурсов из параметров в БД", ex);
        }
    }

    public Schedule create(Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits){
        Schedule schedule = createNewSchedule(arrangementCheckUnits);
        calculateWorkers(schedule, arrangementCheckUnits);
        if(schedule.getSchedulePeriods().size() == 0)
            throw new AS_15_8_PPM_Exception("Ошибка формирования расписания! Не было сформировано ни одного периода!");
        return schedule;
    }

    ArrangementSchedulePeriodProcessing calculateArrangementSchedulePeriodProcessing(
            SortedSet<ScheduleCheckUnit> checkedSet,
            int workersCount,
            String accessTool,
            LocalTime startTime,
            LocalTime endTime){

        ScheduleCheckUnit lastCompletionCheckUnit = checkedSet.first();
        long maxProcessingTime = ChronoUnit.SECONDS.between(startTime, endTime) * workersCount;
        long processingTime = 0;
        for(ScheduleCheckUnit checkUnit : checkedSet){
            long checkUnitProcessingTime = schedulerProperties.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
            if((processingTime + checkUnitProcessingTime) > maxProcessingTime){
                break;
            }
            lastCompletionCheckUnit = checkUnit;
            processingTime += checkUnitProcessingTime;
        }
        return new ArrangementSchedulePeriodProcessing(
                workersCount == 0 ? 1 : Math.max(
                    (int) Math.ceil((double)processingTime / workersCount), 1),
                lastCompletionCheckUnit);
    }

    private Schedule createNewSchedule(Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits){
        Schedule schedule = new Schedule();

        TreeSet<LocalTime> scheduleIntervals = new TreeSet<>();
        for(Map.Entry<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementEntry : arrangementCheckUnits.entrySet()){
            Arrangement arrangement = arrangementEntry.getKey();
            if(arrangementEntry.getValue().isEmpty()){
                throw new AS_15_8_PPM_Exception("Ошибка создания расписания. У мероприятия " + arrangement.getId() + " пустое множество значений для проверки");
            }
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
        return schedule;
    }

    private void calculateWorkers(Schedule schedule, Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits){
        //Готовим карту начальных проверок
        Map<Arrangement, ScheduleCheckUnit> nextCheckUnits = new HashMap<>();
        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
                nextCheckUnits.put(schedulePeriodArrangement.getArrangement(), arrangementCheckUnits.get(schedulePeriodArrangement.getArrangement()).first());
            }
        }

        //обходим периоды
        for(int i = 0; i<schedule.getSchedulePeriods().size(); i++){
            //получаем следующий период (создаем каждый раз новый итератор, т.к. периоды могли удалиться или доавиться на предыдущей ит )
            Iterator<SchedulePeriod> schedulePeriodIterator = schedule.getSchedulePeriods().iterator();
            for(int j = 0; j < i; j++){
                if(schedulePeriodIterator.hasNext())
                    schedulePeriodIterator.next();
            }

            //рассчитываем плотности мероприятий в периоде
            SchedulePeriod schedulePeriod = schedulePeriodIterator.next();
            Map<Arrangement, Double> arrangementDensities = calculateDensities(schedulePeriod, arrangementCheckUnits, nextCheckUnits);
            double totalPeriodDensity = arrangementDensities.values().stream().mapToDouble(Double::doubleValue).sum();

            //Удаляем из периода уже выполненные мероприятия
            Iterator<SchedulePeriodArrangement> schedulePeriodArrangementsIterator = schedulePeriod.getSchedulePeriodArrangements().iterator();
            while(schedulePeriodArrangementsIterator.hasNext()){
                if(nextCheckUnits.get(schedulePeriodArrangementsIterator.next().getArrangement()) == null)
                    schedulePeriodArrangementsIterator.remove();
            }

            //обходим мероприятия периода
            schedulePeriodArrangementsIterator = schedulePeriod.getSchedulePeriodArrangements().iterator();
            Map<Arrangement, ScheduleCheckUnit> notFinishedArrangements = new HashMap<>();
            int busyWorkers = 0;
            boolean isPeriodBreak = false;
            Map<Arrangement, ScheduleCheckUnit> tempNextCheckUnits = new HashMap<>(nextCheckUnits);
            while(schedulePeriodArrangementsIterator.hasNext()){
                //получаем первую проверку в периоде для мероприятия
                SchedulePeriodArrangement schedulePeriodArrangement = schedulePeriodArrangementsIterator.next();
                Arrangement arrangement = schedulePeriodArrangement.getArrangement();
                ScheduleCheckUnit firstCheckUnit = nextCheckUnits.get(arrangement);

                //рассчитываем количество обработчиков мероприятия
                double percent = arrangementDensities.get(arrangement) / totalPeriodDensity;
                int workersCount;
                if(!schedulePeriodArrangementsIterator.hasNext()) {
                    workersCount = this.totalWorkersCount - busyWorkers;
                } else {
                    workersCount = Math.min(Long.valueOf(Math.round(totalWorkersCount * percent)).intValue(), totalWorkersCount - 1);
                    busyWorkers += workersCount;
                }

                //Рассчитываем фактическое время выполнения мероприятия в периоде
                ArrangementSchedulePeriodProcessing schedulePeriodProcessing = calculateArrangementSchedulePeriodProcessing(
                        arrangementCheckUnits.get(arrangement).tailSet(firstCheckUnit),
                        workersCount,
                        arrangement.getAccessTool(),
                        schedulePeriod.getStartTime(),
                        schedulePeriod.getEndTime()
                );

                //фиксируем, с какой обработки начать мероприятие в следующем периоде
                ScheduleCheckUnit nextCheckUnit = arrangementCheckUnits.get(arrangement).higher(schedulePeriodProcessing.getLastCheckUnit());
                tempNextCheckUnits.put(arrangement, nextCheckUnit);

                //Проверяем, выполнится ли мероприятие раньше времени окончания периода
                long schedulePeriodTime = ChronoUnit.SECONDS.between(schedulePeriod.getStartTime(), schedulePeriod.getEndTime());
                if(schedulePeriodTime > schedulePeriodProcessing.getTime()){
                    //разбиваем период, если мероприятия в нем заканчиваются раньше
                    LocalTime breakTime = schedulePeriod.getStartTime().plusSeconds(schedulePeriodProcessing.getTime());
                    SchedulePeriod newSchedulePeriod = new SchedulePeriod(schedule, breakTime, schedulePeriod.getEndTime());
                    for(SchedulePeriodArrangement periodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
                        newSchedulePeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(newSchedulePeriod, periodArrangement.getArrangement()));
                    }
                    schedule.getSchedulePeriods().add(newSchedulePeriod);
                    schedulePeriod.setEndTime(breakTime);

                    isPeriodBreak = true;
                    break;
                }

                //проверяем, остались ли на момент конца периода в мероприятии невыполненные обработки
                if(nextCheckUnit != null){
                    //если период не последний в расписании, то добавляем мероприятие в следующий период
                    // иначе фиксируем, что мероприятие не закончится в пределах текущих периодов расписания
                    if(schedulePeriod != schedule.getSchedulePeriods().last()) {
                        SchedulePeriod nextPeriod = schedule.getSchedulePeriodsAsTreeSet().higher(schedulePeriod);
                        if (nextPeriod!= null && !containsArrangement(nextPeriod.getSchedulePeriodArrangements(), arrangement)) {
                            nextPeriod.getSchedulePeriodArrangements().add(new SchedulePeriodArrangement(nextPeriod, arrangement));
                        }
                    } else {
                        notFinishedArrangements.put(arrangement, nextCheckUnit);
                    }
                }

                schedulePeriodArrangement.setWorkersCount(workersCount);
            }

            // Если период был разбит, то возвращаемся и обрабатываем его заново
            // Иначе фиксируем проверки мероприятий, с которых начнем в следующем периоде
            if(isPeriodBreak){
                i--;
                continue;
            } else {
                nextCheckUnits.clear();
                nextCheckUnits.putAll(tempNextCheckUnits);
            }

            //Если в периоде нет ни одного мероприятия (все закончились раньше), то удаляем период
            if(schedulePeriod.getSchedulePeriodArrangements().size() == 0){
                schedule.getSchedulePeriods().remove(schedulePeriod);
                i--;
            }

            //Если остались мероприятия, не закончившиеся в пределах расписания, то добавляем для них новые периоды
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

    private double calculateDensity(Set<ScheduleCheckUnit> checkUnits, Arrangement arrangement, LocalTime startTime, LocalTime endTime){
        long processingTime = countArrangementProcessingTime(checkUnits, arrangement.getAccessTool());
        long arrangementPlannedDuration = ChronoUnit.SECONDS.between(startTime, endTime);
        double workersCoeff = 1;
        if(arrangement.getMaxWorkersCount() != null)
            workersCoeff = (double) arrangement.getMaxWorkersCount() / this.totalWorkersCount;
        return (double) processingTime / arrangementPlannedDuration * workersCoeff;
    }

    private long countArrangementProcessingTime(Set<ScheduleCheckUnit> checkUnits, String accessTool){
        long processingTime = 0;
        for(ScheduleCheckUnit checkUnit : checkUnits){
            processingTime += schedulerProperties.getProcessingTime(accessTool, checkUnit.getCheckUnitType());
        }
        return processingTime;
    }

    private static boolean containsArrangement(Set<SchedulePeriodArrangement> schedulePeriodArrangements, Arrangement arrangement){
        for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriodArrangements){
            if(schedulePeriodArrangement.getArrangement().equals(arrangement)){
                return true;
            }
        }
        return false;
    }
}
