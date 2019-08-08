package schedule;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import enums.AccessToolUnit;
import model.Arrangement;
import model.ArrangementProcessingPart;
import model.Schedule;
import model.SchedulePeriod;
import org.junit.Test;
import service.ScheduleFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class TestSchedule {

    @Test
    public void testSchedule(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Arrangement arr1 = new Arrangement("arr1", AccessToolUnit.GOOGLE,
                LocalTime.parse("09:00", formatter),
                LocalTime.parse("12:00", formatter));
        generateCheckUnits(arr1, 450000);

        Arrangement arr2 = new Arrangement("arr2", AccessToolUnit.YANDEX,
                LocalTime.parse("10:30", formatter),
                LocalTime.parse("11:30", formatter));
        generateCheckUnits(arr2, 80000);

        Arrangement arr3 = new Arrangement("arr3", AccessToolUnit.KASPERSKY,
                LocalTime.parse("11:15", formatter),
                LocalTime.parse("15:20", formatter));
        generateCheckUnits(arr3, 800000);

        Arrangement arr4 = new Arrangement("arr4", AccessToolUnit.TORGUARD,
                LocalTime.parse("13:10", formatter),
                LocalTime.parse("16:30", formatter));
        generateCheckUnits(arr4, 400000);

        Schedule schedule = ScheduleFactory.create(Arrays.asList(arr1, arr2, arr3, arr4));

        for(SchedulePeriod schedulePeriod : schedule.getSchedulePeriods()){
            System.out.print(formatter.format(schedulePeriod.getStartTime()) + " - " + formatter.format(schedulePeriod.getEndTime()) + ": ");
            for(ArrangementProcessingPart part : schedulePeriod.getArrangementProcessingParts()){
                System.out.print(part.getArrangement().getName() + " " + part.getWorkersCount() + ", ");
            }
            System.out.print("\r\n");
        }
    }

    private void generateCheckUnits(Arrangement arrangement, int count){
        for(int i = 0; i < count; i++){
            arrangement.getCheckUnits().add(new CheckUnit(CheckUnitType.URL, "http://test"+i+".com"));
        }
    }

}
