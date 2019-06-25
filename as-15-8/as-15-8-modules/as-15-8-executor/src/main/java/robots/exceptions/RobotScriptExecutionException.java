package robots.exceptions;

/**
 * Ошибка выполнения скрипта робота
 * @author shabalinAI
 *
 */
public class RobotScriptExecutionException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Ошибка выполнения скрипта робота
	 * @param message Сообщение об ошибке
	 */
	public RobotScriptExecutionException(String message) {
		super(message);
	}
	
	/**
	 * Ошибка выполнения скрипта робота
	 * @param cause Исходная ошибка
	 */
	public RobotScriptExecutionException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Ошибка выполнения скрипта робота
	 * @param message Сообщение об ошибке
	 * @param cause Исходная ошибка
	 */
	public RobotScriptExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
