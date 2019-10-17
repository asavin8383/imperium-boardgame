package exceptions;


//TODO Перенести в единый пакет

public class ExceptionErdiLoad extends Exception {

    private static final long serialVersionUID = 1L;


    public ExceptionErdiLoad(String message) {
        super(message);
    }

    public ExceptionErdiLoad(Throwable cause) {
        super(cause);
    }

    public ExceptionErdiLoad(String message, Throwable cause) {
        super(message, cause);
    }
}
