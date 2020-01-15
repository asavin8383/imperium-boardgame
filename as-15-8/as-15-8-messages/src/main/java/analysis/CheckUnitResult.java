package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.CheckUnitJobResult;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(include= JsonTypeInfo.As.WRAPPER_OBJECT, use= JsonTypeInfo.Id.CLASS)
public class CheckUnitResult {

    /** Статус задания */
    @NonNull
    private CheckUnitJobResult checkResult;

    /** Единица ЕРДИ для проверки */
    @NonNull
    private CheckUnit checkUnit;

    /** Время запуска */
    @NonNull
    private Date startTime;

    /** Время завершения */
    private Date endTime;
}
