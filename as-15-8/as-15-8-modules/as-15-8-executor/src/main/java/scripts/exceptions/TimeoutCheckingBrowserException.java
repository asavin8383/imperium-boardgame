package scripts.exceptions;

public class TimeoutCheckingBrowserException extends TimeoutScriptException {

    private static final long serialVersionUID = -7144492666746821243L;

    public TimeoutCheckingBrowserException(String message) {
        super(message);
    }

    public TimeoutCheckingBrowserException(Throwable cause) {
        super(cause);
    }

    public TimeoutCheckingBrowserException(String message, Throwable cause) {
        super(message, cause);
    }
}
