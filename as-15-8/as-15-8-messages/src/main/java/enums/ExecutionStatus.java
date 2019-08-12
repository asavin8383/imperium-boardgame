package enums;

import lombok.Getter;

public enum ExecutionStatus {
	NEW(0), FORMED(1), SCHEDULED(2), RUNNING(3), ACTION_REQUIRED(4), FINISHED(5), ERROR(6);

	@Getter
	private int priority;

	ExecutionStatus(int priority) {
		this.priority = priority;
	}

}
