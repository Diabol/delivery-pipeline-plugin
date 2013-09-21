package se.diabol.jenkins.pipeline.model.status;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class StatusFactory
{
    public static Status idle() { return new SimpleStatus(StatusType.IDLE,-1, -1); }
    public static Status running(int percentage, long lastActivity, long duration) { return new Running(percentage, lastActivity, duration); }
    public static Status queued(long lastActivity) { return new SimpleStatus(StatusType.QUEUED, lastActivity, -1); }
    public static Status success(long lastActivity, long duration) { return new SimpleStatus(StatusType.SUCCESS, lastActivity, duration); }
    public static Status failed(long lastActivity, long duration) { return new SimpleStatus(StatusType.FAILED, lastActivity, duration); }
    public static Status unstable(long lastActivity, long duration) { return new SimpleStatus(StatusType.UNSTABLE, lastActivity, duration); }
    public static Status cancelled(long lastActivity, long duration) { return new SimpleStatus(StatusType.CANCELLED, lastActivity, duration); }
    public static Status disabled() { return new SimpleStatus(StatusType.DISABLED,-1, -1); }
}
