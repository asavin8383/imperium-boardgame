package model.enums;

import lombok.Getter;

public enum ExecutionStatus {
	NEW(0), PLANNED(1), RUNNING(2), ACTION_REQUIRED(3), FINISHED(4);

	@Getter
	private int priority;

	ExecutionStatus(int priority) {
		this.priority = priority;
	}

}
