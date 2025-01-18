package robots.exceptions;

/**
 * Created by san
 * Date: 28.11.2023
 */
public class BadIP_ExecutionExeption extends ExecutionException{
    private static final long serialVersionUID = 1L;

    public BadIP_ExecutionExeption(String message) {
        super(message);
    }

    public BadIP_ExecutionExeption(Throwable cause) {
        super(cause);
    }

    public BadIP_ExecutionExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
