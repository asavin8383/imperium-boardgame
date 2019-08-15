package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Schedule {

    List<SchedulePeriod> schedulePeriods = new ArrayList<>();

}
