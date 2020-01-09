package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.CheckUnitJobResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(include= JsonTypeInfo.As.WRAPPER_OBJECT, use= JsonTypeInfo.Id.CLASS)
public class CheckUnitResult {

    /** Идентифиатор задания */
    private Long jobID;

    private Long erdiID;

    /** Статус задания */
    private CheckUnitJobResult checkResult;

    /** Единица ЕРДИ для проверки */
    private CheckUnit checkUnit;
}
