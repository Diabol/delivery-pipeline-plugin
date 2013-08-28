package se.diabol.jenkins.pipeline.model.status;


/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public interface Status
{
    public boolean isRunning();

    public boolean isSuccess();

    public boolean isFailed();

    public boolean isIdle();

    public boolean isStarted();

    public int getPercentage(); // todo: This is really only for Running status.
}
