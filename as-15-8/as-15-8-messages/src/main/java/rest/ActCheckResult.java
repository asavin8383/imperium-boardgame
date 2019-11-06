package rest;

import checkUnits.CheckUnitType;
import lombok.Data;

@Data
public class ActCheckResult {
    public Long checkResultId;
    public CheckUnitType checkUnitType;
    public String checkUnitValue;
    public String date;    // 2019-08-15T18:58:00
}
