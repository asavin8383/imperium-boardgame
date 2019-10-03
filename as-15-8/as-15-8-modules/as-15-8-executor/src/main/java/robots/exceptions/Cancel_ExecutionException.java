package robots.exceptions;

public class Cancel_ExecutionException extends ExecutionException {

	private static final long serialVersionUID = 1L;

	public Cancel_ExecutionException(String message) {
        super(message);
    }
	
    public Cancel_ExecutionException(Throwable cause) {
        super(cause);
    }
}
