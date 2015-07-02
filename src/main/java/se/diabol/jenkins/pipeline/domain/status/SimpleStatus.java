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
package se.diabol.jenkins.pipeline.domain.status;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.status.promotion.AbstractPromotionStatusProvider;
import se.diabol.jenkins.pipeline.domain.status.promotion.PromotionStatus;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static se.diabol.jenkins.pipeline.domain.status.StatusType.*;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class SimpleStatus implements Status {
    private final StatusType type;
    private final long lastActivity;
    private final long duration;

    private final boolean promoted;
	private final List<PromotionStatus> promotions;

    private static PromotionStatusProviderWrapper promotionStatusProviderWrapper = new PromotionStatusProviderWrapper();

    public SimpleStatus(StatusType type, long lastActivity, long duration) {
        this(type, lastActivity, duration, false, Collections.<PromotionStatus>emptyList());
    }

    public SimpleStatus(StatusType type, long lastActivity, long duration, boolean promoted, List<PromotionStatus> promotions) {
        this.type = type;
        this.lastActivity = lastActivity;
        this.duration = duration;
		this.promoted = promoted;
		this.promotions = promotions;
    }

	@Exported
	public List<PromotionStatus> getPromotions()
	{
		return promotions;
	}

    @Exported
	@Override
	public boolean isPromoted()
	{
		return promoted;
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
    public boolean isNotBuilt() {
        return NOT_BUILT.equals(type);
    }


    @Override
    public boolean isDisabled() {
        return DISABLED.equals(type);
    }

    public static Status resolveStatus(AbstractProject project, AbstractBuild build, AbstractBuild firstBuild) {
        if (build == null) {
            if (ProjectUtil.isQueued(project, firstBuild)) {
                return StatusFactory.queued(project.getQueueItem().getInQueueSince());
            } else if (project.isDisabled()) {
                return StatusFactory.disabled();
            } else {
                return StatusFactory.idle();
            }
        }

        if (build.isBuilding()) {
            int progress = (int) round(100.0d * (currentTimeMillis() - build.getTimestamp().getTimeInMillis())
                                / build.getEstimatedDuration());
            if (progress > 100) {
                progress = 99;
            }

            return StatusFactory.running(progress, build.getTimeInMillis(), currentTimeMillis() - build.getTimestamp().getTimeInMillis());
        }
        return getStatusFromResult(build);
    }

    private static Status getStatusFromResult(AbstractBuild build) {
        Result result = build.getResult();
        if (Result.ABORTED.equals(result)) {
            return StatusFactory.cancelled(build.getTimeInMillis(), build.getDuration());
        }
        if (Result.SUCCESS.equals(result)) {
            return StatusFactory.success(build.getTimeInMillis(), build.getDuration(), isBuildPromoted(build), getPromotionStatusList(build));
        }
        if (Result.FAILURE.equals(result)) {
            return StatusFactory.failed(build.getTimeInMillis(), build.getDuration(), isBuildPromoted(build), getPromotionStatusList(build));
        }
        if (Result.UNSTABLE.equals(result)) {
            return StatusFactory.unstable(build.getTimeInMillis(), build.getDuration());
        }
        if (Result.NOT_BUILT.equals(result)) {
            return StatusFactory.notBuilt(build.getTimeInMillis(), build.getDuration());
        }
        throw new IllegalStateException("Result " + result + " not recognized.");
    }

    private static boolean isBuildPromoted(AbstractBuild build) {
        final List<AbstractPromotionStatusProvider> promotionStatusProviders = SimpleStatus.promotionStatusProviderWrapper.getAllPromotionStatusProviders();
        if(CollectionUtils.isNotEmpty(promotionStatusProviders)) {
            final AbstractPromotionStatusProvider promotionStatusProvider = promotionStatusProviders.get(0);
            if (promotionStatusProvider != null) {
                return promotionStatusProvider.isBuildPromoted(build);
            }
        }
        return false;
    }

    private static List<PromotionStatus> getPromotionStatusList(AbstractBuild build) {
        final List<PromotionStatus> promotionStatusList = new ArrayList<PromotionStatus>();

        final List<AbstractPromotionStatusProvider> promotionStatusProviders = SimpleStatus.promotionStatusProviderWrapper.getAllPromotionStatusProviders();
        if(CollectionUtils.isNotEmpty(promotionStatusProviders)) {
            final AbstractPromotionStatusProvider promotionStatusProvider = promotionStatusProviders.get(0);
            if (promotionStatusProvider != null) {
                promotionStatusList.addAll(promotionStatusProvider.getPromotionStatusList(build));
            }
        }
        return promotionStatusList;
    }

    @Override
    public String toString() {
        return String.valueOf(type);
    }

    // Decorators to make code unit-testable

    static class PromotionStatusProviderWrapper {
        public List<AbstractPromotionStatusProvider> getAllPromotionStatusProviders() {
            return AbstractPromotionStatusProvider.all();
        }
    }

    // package scope setters for unit testing

    static void setPromotionStatusProviderWrapper(PromotionStatusProviderWrapper promotionStatusProviderWrapper) {
        SimpleStatus.promotionStatusProviderWrapper = promotionStatusProviderWrapper;
    }
}
