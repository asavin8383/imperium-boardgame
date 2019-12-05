package robots.exceptions;

public class Timeout_ExecutionException extends ExecutionException {

	private static final long serialVersionUID = 1L;

    public Timeout_ExecutionException() {
        super("");
    }

	public Timeout_ExecutionException(String message) {
        super(message);
    }

    public Timeout_ExecutionException(Throwable cause) {
        super(cause);
    }
}
