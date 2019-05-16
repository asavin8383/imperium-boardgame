package exceptions;

/**
 * Исключение АС 15.8
 */

public class AS_15_8_Exception extends RuntimeException {

    public AS_15_8_Exception(String s) {
        super(s);
    }

    public AS_15_8_Exception(String s, Throwable t){
        super(s,t);
    }


}
