package model;

import lombok.Data;

import java.util.Comparator;
import java.util.TreeSet;

@Data
public class Schedule {

    TreeSet<SchedulePeriod> schedulePeriods = new TreeSet<>(Comparator.comparing(SchedulePeriod::getStartTime));

}
