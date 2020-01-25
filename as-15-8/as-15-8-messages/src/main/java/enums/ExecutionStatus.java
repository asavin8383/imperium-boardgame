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
	FINISHED(6, "Выполнено"),
	ACT_SENT(7, "Отправлен акт"),
	CLOSED(8, "Закрыто"),
	ERROR(9, "Ошибка");

	@Getter
	private int priority;
	@Getter
	private String description;



}
