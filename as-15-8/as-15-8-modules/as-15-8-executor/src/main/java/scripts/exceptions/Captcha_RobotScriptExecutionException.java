package scripts.exceptions;

import scripts.RobotScriptExecutionException;

public class Captcha_RobotScriptExecutionException extends RobotScriptExecutionException {

	private static final long serialVersionUID = 1L;

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
