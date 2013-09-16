package se.diabol.jenkins.pipeline.model.status;

import org.apache.log4j.helpers.ISO8601DateFormat;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static se.diabol.jenkins.pipeline.model.status.StatusType.*;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean(defaultVisibility = 100)
public class SimpleStatus implements Status {
    private final StatusType type;
    private final long lastActivity;

    public SimpleStatus(StatusType type, long lastActivity) {
        this.type = type;
        this.lastActivity = lastActivity;
    }

    @Exported
    @SuppressWarnings("unused")
    public StatusType getType() {
        return type;
    }

    @Override
    public long getLastActivity() {
        return lastActivity;
    }

    @Exported
    @SuppressWarnings("unused")
    public String getTimestamp() {
        if (lastActivity != -1) {
        DateFormat f = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
        return f.format(new Date(lastActivity));
        } else {
            return null;
        }
    }

    @Override
    public boolean isIdle() {
        return IDLE.equals(type);
    }

    @Override
    public boolean isQueued() {
        return QUEUED.equals(type);
    }

    @Override
    public boolean isRunning() {
        return RUNNING.equals(type);
    }

    @Override
    public boolean isSuccess() {
        return SUCCESS.equals(type);
    }

    @Override
    public boolean isFailed() {
        return FAILED.equals(type);
    }

    @Override
    public boolean isUnstable() {
        return UNSTABLE.equals(type);
    }

    @Override
    public boolean isCancelled() {
        return CANCELLED.equals(type);
    }

    @Override
    public boolean isDisabled() {
        return DISABLED.equals(type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SimpleStatus && Objects.equals(((SimpleStatus) obj).type, type);
    }

    @Override
    public String toString() {
        return String.valueOf(type);
    }
}
