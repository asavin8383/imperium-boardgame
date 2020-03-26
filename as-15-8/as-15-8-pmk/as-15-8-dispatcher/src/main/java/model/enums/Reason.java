package model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Reason {
    STOPPED_BY_SERVICE_MODE("Остановлено при переходе в сервисный режим"),
    STOPPED_BY_MAX_CHECK_UNITS_COUNT("Отсановлено по превышению ИРТЗ"),
    MANUAL("Остановлено пользователем"),
    STOPPED_BY_DAY_GONE("Остановлено по завершению дня"),
    NORMAL("Завершено штатно");

    @Getter
    private String description;
}
