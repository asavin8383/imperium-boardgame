package exceptions;

import org.slf4j.Logger;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Исключение, выбрасываемое диспетчером
 */

public class AS_15_8_DispatcherException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AS_15_8_DispatcherException(String message) {
        super(message);
    }

    public AS_15_8_DispatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AS_15_8_DispatcherException logAndGet(Logger logger, String message) {
        AS_15_8_DispatcherException e = new AS_15_8_DispatcherException(message);
        // log the stack trace as well
        logger.error(message);
        return e;
    }

    public static AS_15_8_DispatcherException logAndGet(Logger logger, String message, Throwable ex) {
        AS_15_8_DispatcherException e = new AS_15_8_DispatcherException(message);
        // log the stack trace as well
        logger.error(message, ex);
        return e;
    }
}
