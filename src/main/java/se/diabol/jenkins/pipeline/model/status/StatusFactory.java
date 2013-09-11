package se.diabol.jenkins.pipeline.model.status;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class StatusFactory
{
    public static Status idle() { return new SimpleStatus(StatusType.IDLE,0); }
    public static Status running(int percentage, long lastActivity) { return new Running(percentage, lastActivity); }
    public static Status queued(long lastActivity) { return new SimpleStatus(StatusType.QUEUED, lastActivity); }
    public static Status success(long lastActivity) { return new SimpleStatus(StatusType.SUCCESS, lastActivity); }
    public static Status failed(long lastActivity) { return new SimpleStatus(StatusType.FAILED, lastActivity); }
    public static Status unstable(long lastActivity) { return new SimpleStatus(StatusType.UNSTABLE, lastActivity); }
    public static Status cancelled(long lastActivity) { return new SimpleStatus(StatusType.CANCELLED, lastActivity); }
    public static Status disabled() { return new SimpleStatus(StatusType.DISABLED,0); }
}
