package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArrangementSchedulePeriodProcessing {
    private long time;
    private ScheduleCheckUnit lastCheckUnit;
}
