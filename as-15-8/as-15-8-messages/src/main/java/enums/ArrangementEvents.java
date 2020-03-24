package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * События, вызывающие переход между состояниями мероприятия
 * Creation date: 09.08.2019
 * Author: asavin
 */
@AllArgsConstructor
public enum ArrangementEvents {
    FILL("FILL"),
    SCHEDULE("SCHEDULE"),
    SCHEDULE_ROLLBACK("SCHEDULE_ROLLBACK"),
    RUN("RUN"),
    FINISH("FINISH"),
    PREPARE_TO_STOP("PREPARE_TO_STOP"),
    STOP("Остановлено пользователем"),
    SEND_ACT("SEND_ACT"),
    STOP_BY_SERVICE_MODE("Остановлено при переходе в сервисный режим"),
    STOP_BY_MAX_CHECK_UNITS_COUNT("Остановлено по превышению количества ИРТЗ"),
    STOP_BY_DAY_GONE("Остановлено по завершению дня"),
    MANUAL_SCHEDULE("MANUAL_SCHEDULE");

    @Getter
    private String description;
}

