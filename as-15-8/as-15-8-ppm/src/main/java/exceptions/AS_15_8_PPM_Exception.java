package exceptions;

import org.slf4j.Logger;

/**
 * Created by san
 * Date: 03.11.2019
 * Исключение ППМ
 */
public class AS_15_8_PPM_Exception extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AS_15_8_PPM_Exception(String s) {
        super(s);
    }

    public AS_15_8_PPM_Exception(String s, Throwable t){
        super(s,t);
    }

    public static AS_15_8_PPM_Exception logAndGet(Logger logger, String message) {
        AS_15_8_PPM_Exception e = new AS_15_8_PPM_Exception(message);
        // log the stack trace as well
        logger.error(message);
        return e;
    }
}
