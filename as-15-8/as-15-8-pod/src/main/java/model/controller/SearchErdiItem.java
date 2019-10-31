package model.controller;

import checkUnits.CheckUnitType;
import lombok.Data;


@Data
public class SearchErdiItem {
    private Long erdiId;
    private CheckUnitType checkUnitType;
    private String checkUnitValue;
}