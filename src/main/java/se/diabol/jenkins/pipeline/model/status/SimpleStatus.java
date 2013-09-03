package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Objects;

import static se.diabol.jenkins.pipeline.model.status.StatusType.*;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean(defaultVisibility = 100)
public class SimpleStatus implements Status
{
    private final StatusType statusType;

    public SimpleStatus(StatusType statusType)
    {
        this.statusType = statusType;
    }

    @Exported
    public StatusType getStatusType()
    {
        return statusType;
    }

    @Override public boolean isIdle() { return IDLE.equals(statusType); }
    @Override public boolean isQueued() { return QUEUED.equals(statusType); }
    @Override public boolean isRunning() { return RUNNING.equals(statusType); }
    @Override public boolean isSuccess() { return SUCCESS.equals(statusType); }
    @Override public boolean isFailed() { return FAILED.equals(statusType); }
    @Override public boolean isUnstable() { return UNSTABLE.equals(statusType); }
    @Override public boolean isCancelled() { return CANCELLED.equals(statusType); }

    @Override
    public int hashCode()
    {
        return statusType.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj instanceof SimpleStatus && Objects.equals(((SimpleStatus) obj).statusType, statusType);
    }

    @Override
    public String toString()
    {
        return String.valueOf(statusType);
    }
}
