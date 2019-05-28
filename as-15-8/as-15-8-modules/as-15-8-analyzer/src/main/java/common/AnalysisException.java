package common;

/**
 * Ошибка выполнения анализа
 * @author shabalinAI
 *
 */
public class AnalysisException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Ошибка выполнения анализа
	 * @param message Сообщение об ошибке
	 */
	public AnalysisException(String message) {
		super(message);
	}
	
	/**
	 * Ошибка выполнения анализа
	 * @param message Сообщение об ошибке
	 * @param cause Исходная ошибка
	 */
	public AnalysisException(String message, Throwable cause) {
		super(message, cause);
	}
}
