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
    private final StatusType type;

    public SimpleStatus(StatusType type)
    {
        this.type = type;
    }

    @Exported
    @SuppressWarnings("unused")
    public StatusType getType()
    {
        return type;
    }

    @Override public boolean isIdle() { return IDLE.equals(type); }
    @Override public boolean isQueued() { return QUEUED.equals(type); }
    @Override public boolean isRunning() { return RUNNING.equals(type); }
    @Override public boolean isSuccess() { return SUCCESS.equals(type); }
    @Override public boolean isFailed() { return FAILED.equals(type); }
    @Override public boolean isUnstable() { return UNSTABLE.equals(type); }
    @Override public boolean isCancelled() { return CANCELLED.equals(type); }

    @Override
    public int hashCode()
    {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj instanceof SimpleStatus && Objects.equals(((SimpleStatus) obj).type, type);
    }

    @Override
    public String toString()
    {
        return String.valueOf(type);
    }
}
