package se.diabol.jenkins.pipeline.model.status;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class StatusFactory
{
    public static Status running(int percentage) { return new Running(percentage); }
    public static Status cancelled() { return new SimpleStatus(StatusType.CANCELLED); }
    public static Status success() { return new SimpleStatus(StatusType.SUCCESS); }
    public static Status failed() { return new SimpleStatus(StatusType.FAILED); }
    public static Status unstable() { return new SimpleStatus(StatusType.UNSTABLE); }
    public static Status idle() { return new SimpleStatus(StatusType.IDLE); }
    public static Status disabled() { return new SimpleStatus(StatusType.DISABLED); }
}
