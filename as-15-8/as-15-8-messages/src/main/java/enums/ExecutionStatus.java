package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExecutionStatus {
	RUNNING(0, "Выполняется"),
	NEW(1, "Новое"),
	FORMED(2, "Готово к включению в расписание"),
	SCHEDULED(3, "Запланировано"),
	ACTION_REQUIRED(4, "Требуется действие пользователя"),
	FINISHED(5, "Выполнено"),
	ACT_SENT(6, "Отправлен акт"),
	CLOSED(7, "Закрыто"),
	ERROR(8, "Ошибка");

	@Getter
	private int priority;
	@Getter
	private String description;



}
