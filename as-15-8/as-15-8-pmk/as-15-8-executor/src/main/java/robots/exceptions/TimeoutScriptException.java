package robots.exceptions;

public class TimeoutScriptException extends ExecutionException {

    private static final long serialVersionUID = -7144492788746821243L;

    public TimeoutScriptException(String message) {
        super(message);
    }

    public TimeoutScriptException(Throwable cause) {
        super(cause);
    }

    public TimeoutScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
