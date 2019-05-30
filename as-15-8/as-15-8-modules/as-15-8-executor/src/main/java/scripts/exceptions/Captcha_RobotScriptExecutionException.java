package scripts.exceptions;

import scripts.RobotScriptExecutionException;

public class Captcha_RobotScriptExecutionException extends RobotScriptExecutionException {

    public Captcha_RobotScriptExecutionException(String message) {
        super(message);
    }

    public Captcha_RobotScriptExecutionException(Throwable cause) {
        super(cause);
    }

    public Captcha_RobotScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
