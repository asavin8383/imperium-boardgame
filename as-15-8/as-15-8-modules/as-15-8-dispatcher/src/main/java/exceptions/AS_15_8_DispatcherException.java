package exceptions;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Исключение, выбрасываемое диспетчером
 */

public class AS_15_8_DispatcherException extends RuntimeException {

    public AS_15_8_DispatcherException(String message) {
        super(message);
    }

    public AS_15_8_DispatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
