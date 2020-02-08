package model.enums;

import java.awt.*;

/**
 * Результат проверки запрещенного ресурса, установленный пользователем
 * @author shabalinAI
 *
 */
public enum UserResult {

	COMPLETED,
	DOUBTFUL,
	FORBIDDEN_CONTENT_DETECTED;

	public static boolean contains(String value) {
		for (UserResult c : UserResult.values()) {
			if (c.name().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
}
