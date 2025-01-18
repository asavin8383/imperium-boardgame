package robots.exceptions;

public class CloudflareBlockExecutionException extends ExecutionException {

    private static final long serialVersionUID = 1L;


    public CloudflareBlockExecutionException(String message) {
        super(message);
    }
}
