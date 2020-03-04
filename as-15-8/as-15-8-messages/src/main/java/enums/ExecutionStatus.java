package enums;

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
	FINISHED(8, "Выполнено"),
	ACT_SENT(9, "Отправлен акт"),
	CLOSED(10, "Закрыто"),
	ERROR(11, "Ошибка");

	@Getter
	private int priority;
	@Getter
	private String description;



}
