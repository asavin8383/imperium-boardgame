package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.CaseFormat;
import enums.CheckUnitJobResult;
import lombok.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;

import static com.sun.org.glassfish.external.statistics.impl.StatisticImpl.START_TIME;

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
