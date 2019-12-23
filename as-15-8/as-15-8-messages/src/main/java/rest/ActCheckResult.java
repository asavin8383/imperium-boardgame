package rest;

import checkUnits.CheckUnitType;
import lombok.Data;

@Data
public class ActCheckResult {
    private Long checkResultId;
    private CheckUnitType checkUnitType;
    private String checkUnitValue;
    private String date;    // 2019-08-15T18:58:00
    private Long contentId;
    private boolean forbiddenContentDetected;
}
