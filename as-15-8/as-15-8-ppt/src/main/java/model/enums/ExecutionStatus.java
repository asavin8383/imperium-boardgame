package model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExecutionStatus {

	RUNNING(0, "Выполняется"),
	NEW(1, "Новое"),
	FORMED(2, "Готово к включению в расписание"),
	SCHEDULED(3, "Запланировано"),
	STOPPING(4, "Останавливается"),
	STOPPED(5, "Остановлено"),
	STOPPED_BY_SERVICE_MODE(6,"Остановлено при переходе в сервисный режим"),
	STOPPED_BY_MAX_CHECK_UNITS(7, "Остановлено по превышению количества ИРТЗ"),
	STOPPED_BY_DAY_GONE(8, "Остановлено по завершению дня"),
	FINISHED(9, "Выполнено"),
	ACT_SENT(10, "Отправлен акт");

	@Getter
	private int priority;
	@Getter
	private String description;



}
