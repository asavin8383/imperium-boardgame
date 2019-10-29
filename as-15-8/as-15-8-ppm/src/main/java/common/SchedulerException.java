package common;

public class SchedulerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SchedulerException(String s) {
        super(s);
    }

    public SchedulerException(String s, Throwable t){
        super(s,t);
    }
}