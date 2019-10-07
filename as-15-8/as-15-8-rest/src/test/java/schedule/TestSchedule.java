package schedule;

import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import enums.AccessToolUnit;
import model.catalog.AccessTool;
import model.result.ArrangementResult;
import model.schedule.Schedule;
import model.schedule.ScheduleCheckUnit;
import model.schedule.SchedulePeriod;
import model.schedule.SchedulePeriodArrangement;
import model.task.Arrangement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.PlannedProcessingTimeRepo;
import services.schedule.PlannedProcessingTimeService;
import services.schedule.ScheduleService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("classpath:application.yml")
public class TestSchedule {

    @Autowired
    private ScheduleService scheduleService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Test
    public void testSchedule(){

        Map<Arrangement, TreeSet<ScheduleCheckUnit>> scheduleArrangements = new HashMap<>();

        scheduleArrangements.put(
            createArrangement("arr1", AccessToolUnit.GOOGLE, "02:51","11:30"),
            generateCheckUnits( 1000));

        /*scheduleArrangements.put(
            createArrangement("arr2", AccessToolUnit.YANDEX,"09:00", "10:00"),
            generateCheckUnits(3000));

        scheduleArrangements.put(
            createArrangement("arr3", AccessToolUnit.KASPERSKY,"11:15", "15:20"),
            generateCheckUnits(40000));

        scheduleArrangements.put(
            createArrangement("arr4", AccessToolUnit.TORGUARD,"13:10", "16:30"),
            generateCheckUnits(25000));*/

        Schedule schedule = scheduleService.create(scheduleArrangements);

        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            System.out.print(formatter.format(schedulePeriod.getStartTime()) + " - " + formatter.format(schedulePeriod.getEndTime()) + ": ");
            for(SchedulePeriodArrangement schedulePeriodArrangement : schedulePeriod.getSchedulePeriodArrangements()){
                System.out.print(schedulePeriodArrangement.getArrangement().getTitle() + " " + schedulePeriodArrangement.getWorkersCount() + ", ");
            }
            System.out.print("\r\n");
        }
    }

    private Arrangement createArrangement(String name, AccessToolUnit accessToolUnit, String startTime, String endTime){
        AccessTool accessTool = new AccessTool();
        accessTool.setName(accessToolUnit);

        Arrangement arrangement = new Arrangement();
        arrangement.setTitle(name);
        arrangement.setAccessTool(accessTool);
        arrangement.setPlannedStartTime(LocalTime.parse(startTime, formatter));
        arrangement.setPlannedEndTime(LocalTime.parse(endTime, formatter));
        return arrangement;
    }

    private TreeSet<ScheduleCheckUnit> generateCheckUnits(int count){
        TreeSet<ScheduleCheckUnit> checkUnits = new TreeSet<>(Comparator.comparing(ScheduleCheckUnit::getCheckUnitValue));
        for(int i = 0; i < count; i++){
            ScheduleCheckUnit checkUnit = new ScheduleCheckUnit();
            checkUnit.setCheckUnitType(CheckUnitType.URL);
            checkUnit.setCheckUnitValue("http://test"+i+".com");
            checkUnits.add(checkUnit);
        }
        return checkUnits;
    }

}
