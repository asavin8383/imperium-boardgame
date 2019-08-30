package exceptions;


//TODO Перенести в единый пакет

public class ExceptionErdiParser extends Exception {

    private static final long serialVersionUID = 1L;


    public ExceptionErdiParser(String message) {
        super(message);
    }

    public ExceptionErdiParser(Throwable cause) {
        super(cause);
    }

    public ExceptionErdiParser(String message, Throwable cause) {
        super(message, cause);
    }
}
