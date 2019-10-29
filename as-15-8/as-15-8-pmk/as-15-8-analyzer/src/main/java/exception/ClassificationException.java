package exception;

/**
 * Creation date: 16.07.2019
 * Author: asavin
 */
public class ClassificationException extends RuntimeException {
    public ClassificationException(String message) {
        super(message);
    }

    public ClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
