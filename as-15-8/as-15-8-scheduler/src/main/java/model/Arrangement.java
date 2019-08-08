package model;

import checkUnits.CheckUnit;
import enums.AccessToolUnit;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.TreeSet;

@Data
public class Arrangement {

    @NonNull
    private String name;
    @NonNull
    private AccessToolUnit type;

    TreeSet<CheckUnit> checkUnits = new TreeSet<>(Comparator.comparing(CheckUnit::getValue));

    @NonNull
    private LocalTime plannedStartDate;
    @NonNull
    private LocalTime plannedEndDate;

}
