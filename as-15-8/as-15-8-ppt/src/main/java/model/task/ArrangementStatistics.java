package model.task;

import enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by san
 * Date: 08.11.2019
 */
@Data
@AllArgsConstructor
public class ArrangementStatistics {
    private ExecutionStatus executionStatus;
    private Long number;
}
