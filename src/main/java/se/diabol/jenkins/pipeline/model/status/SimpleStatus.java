/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.model.AbstractItem;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

import static se.diabol.jenkins.pipeline.model.status.StatusType.*;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class SimpleStatus implements Status {
    private final StatusType type;
    private final long lastActivity;
    private final long duration;

    public SimpleStatus(StatusType type, long lastActivity, long duration) {
        this.type = type;
        this.lastActivity = lastActivity;
        this.duration = duration;
    }

    @Exported
    public StatusType getType() {
        return type;
    }

    @Override
    public long getLastActivity() {
        return lastActivity;
    }

    @Exported
    public String getTimestamp() {
        if (lastActivity != -1) {
            return PipelineUtils.formatTimestamp(lastActivity);
        } else {
            return null;
        }
    }

    @Exported
    @Override
    public long getDuration() {
        return duration;
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
        return this == obj || obj instanceof SimpleStatus && type.equals(((SimpleStatus) obj).getType());
    }

    @Override
    public String toString() {
        return String.valueOf(type);
    }
}
