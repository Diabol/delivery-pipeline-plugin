package se.diabol.jenkins.pipeline.model.status;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class StatusFactory
{
    public static Status valueOf(String v)
    {
        if (v.startsWith("RUNNING "))
            return running(25); // todo: Acutally parse percentage
        else
            return StatusEnum.valueOf(v);
    }

    private enum StatusEnum implements Status
    {
        SUCCESS, FAILED, UNSTABLE, CANCELLED, DISABLED, IDLE;

        @Override public boolean isRunning() { return false; }
        @Override public boolean isSuccess() { return this == SUCCESS; }
        @Override public boolean isFailed() { return this == FAILED; }
        @Override public boolean isIdle() { return this == IDLE; }
        @Override public boolean isStarted() { return isRunning() || isSuccess() || isFailed(); }
        @Override public int getPercentage() { return 0; /* todo: remove from status */ }
    }

    public static Status running(int percentage) { return new Running(percentage); }
    public static Status cancelled() { return StatusEnum.CANCELLED; }
    public static Status success() { return StatusEnum.SUCCESS; }
    public static Status failed() { return StatusEnum.FAILED; }
    public static Status unstable() { return StatusEnum.UNSTABLE; }
    public static Status idle() { return StatusEnum.IDLE; }
}
