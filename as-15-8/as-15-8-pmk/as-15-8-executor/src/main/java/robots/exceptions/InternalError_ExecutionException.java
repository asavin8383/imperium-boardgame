package robots.exceptions;

/**
 * Created by san
 * Date: 05.12.2023
 */
public class InternalError_ExecutionException extends ExecutionException{

    private static final long serialVersionUID = 1L;

    public InternalError_ExecutionException(String message) {
        super(message);
    }

    public InternalError_ExecutionException(Throwable cause) {
        super(cause);
    }

    public InternalError_ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
