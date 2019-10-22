package robots.exceptions;

public class Captcha_ExecutionException extends ExecutionException {

	private static final long serialVersionUID = 1L;

	public Captcha_ExecutionException(String message) {
        super(message);
    }

    public Captcha_ExecutionException(Throwable cause) {
        super(cause);
    }

    public Captcha_ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
