package analysis;

import checkUnits.CheckUnit;
import enums.CheckUnitJobResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class CheckUnitResult {

    /** Идентифиатор задания */
    private Long jobID;

    private Long erdiID;

    /** Статус задания */
    private CheckUnitJobResult checkResult;

    /** Единица ЕРДИ для проверки */
    private CheckUnit checkUnit;
}
