package model.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import model.enums.ExecutionStatus;

/**
 * Created by san
 * Date: 08.11.2019
 */
@Data
@AllArgsConstructor
public class ExecutionStatusStatistics {
    private ExecutionStatus executionStatus;
    private Long number;
}
