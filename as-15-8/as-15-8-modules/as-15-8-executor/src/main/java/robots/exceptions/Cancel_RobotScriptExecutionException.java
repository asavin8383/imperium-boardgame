package robots.exceptions;

public class Cancel_RobotScriptExecutionException extends RobotScriptExecutionException {

	private static final long serialVersionUID = 1L;

	public Cancel_RobotScriptExecutionException(String message) {
        super(message);
    }
	
    public Cancel_RobotScriptExecutionException(Throwable cause) {
        super(cause);
    }
}
