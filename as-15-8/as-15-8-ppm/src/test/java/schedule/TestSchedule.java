package schedule;

import checkUnits.CheckUnitType;
import common.SchedulerApplicationConfiguration;
import model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import services.ScheduleCreationService;
import services.ScheduleService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SchedulerApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class TestSchedule {

    @Autowired
    private ScheduleCreationService scheduleCreationService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Test
    public void testSchedule(){

        Map<Arrangement, TreeSet<ScheduleCheckUnit>> scheduleArrangements = new HashMap<>();

        scheduleArrangements.put(
                createArrangement("arr1", "kaspersky", "11:00:00","12:10:00"),
                generateCheckUnits( 19524, CheckUnitType.IP_V4));

        /*scheduleArrangements.put(
            createArrangement("arr2", AccessToolUnit.YANDEX,"09:00", "10:00"),
            generateCheckUnits(3000));

        scheduleArrangements.put(
            createArrangement("arr3", AccessToolUnit.KASPERSKY,"11:15", "15:20"),
            generateCheckUnits(40000));

        scheduleArrangements.put(
            createArrangement("arr4", AccessToolUnit.TORGUARD,"13:10", "16:30"),
            generateCheckUnits(25000));*/

        Schedule schedule = scheduleCreationService.create(scheduleArrangements, 20);

        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            System.out.print(formatter.format(schedulePeriod.getStartTime()) + " - " + formatter.format(schedulePeriod.getEndTime()) + ": ");
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
                System.out.print(schedulePeriodArrangement.getArrangement().getTitle() + " " + schedulePeriodArrangement.getWorkersCount());
            }
            System.out.print("\r\n");
        }
    }

    private Arrangement createArrangement(String name, String accessTool, String startTime, String endTime){
        Arrangement arrangement = new Arrangement();
        arrangement.setId(999999999999L);
        arrangement.setTitle(name);
        arrangement.setAccessTool(accessTool);
        arrangement.setPlannedStartTime(LocalTime.parse(startTime, formatter));
        arrangement.setPlannedEndTime(LocalTime.parse(endTime, formatter));
        arrangement.setIsScheduled(false);
        return arrangement;
    }

    private TreeSet<ScheduleCheckUnit> generateCheckUnits(int count, CheckUnitType checkUnitType){
        TreeSet<ScheduleCheckUnit> checkUnits = new TreeSet<>(Comparator.comparing(ScheduleCheckUnit::getCheckUnitValue));
        for(int i = 0; i < count; i++){
            ScheduleCheckUnit checkUnit = new ScheduleCheckUnit();
            checkUnit.setCheckUnitType(checkUnitType);
            checkUnit.setCheckUnitValue("http://test"+i+".com");
            checkUnits.add(checkUnit);
        }
        return checkUnits;
    }

}