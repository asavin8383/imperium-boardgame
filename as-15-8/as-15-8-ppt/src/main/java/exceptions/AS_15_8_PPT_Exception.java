package exceptions;

import org.slf4j.Logger;

/**
 * Исключение АС 15.8
 */

public class AS_15_8_PPT_Exception extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AS_15_8_PPT_Exception(String s) {
        super(s);
    }

    public AS_15_8_PPT_Exception(String s, Throwable t){
        super(s,t);
    }

    public static AS_15_8_PPT_Exception logAndGet(Logger logger, String message) {
        AS_15_8_PPT_Exception e = new AS_15_8_PPT_Exception(message);
        // log the stack trace as well
        logger.error(message);
        return e;
    }

    public static AS_15_8_PPT_Exception logAndGet(Logger logger, String message, Throwable ex) {
        AS_15_8_PPT_Exception e = new AS_15_8_PPT_Exception(message, ex);
        // log the stack trace as well
        logger.error(message, ex);
        return e;
    }
}
