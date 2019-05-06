package exceptions;

/**
 * Исключение, возникающее в том случае, когда запрашиваемый объект не найден в БД
 * @author asavin
 *
 */

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ResourceNotFoundException() {
		super();
	}
	
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
