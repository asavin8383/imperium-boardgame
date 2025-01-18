package captcha.solver;

public class CaptchaSolverException extends Exception {

    private static final long serialVersionUID = 1L;

    public CaptchaSolverException(String message) {
        super(message);
    }

    public CaptchaSolverException(Throwable cause) {
        super(cause);
    }

    public CaptchaSolverException(String message, Throwable cause) {
        super(message, cause);
    }
}
