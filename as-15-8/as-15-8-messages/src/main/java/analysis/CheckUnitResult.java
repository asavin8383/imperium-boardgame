package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.CheckUnitJobResult;
import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime startTime;

    /** Время завершения */
    private LocalDateTime endTime;
}
