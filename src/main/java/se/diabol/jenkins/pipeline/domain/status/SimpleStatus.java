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

import hudson.model.*;
import hudson.plugins.promoted_builds.PromotedBuildAction;
import hudson.plugins.promoted_builds.Promotion;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.*;

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

    private static PromotionStatusDecorator promotionStatusDecorator = new PromotionStatusDecorator();
    private static PromotedBuildActionDecorator promotedBuildActionDecorator = new PromotedBuildActionDecorator();

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
            return getStatusWithPromotionsFromSuccessResult(build);
        }
        if (Result.FAILURE.equals(result)) {
            return StatusFactory.failed(build.getTimeInMillis(), build.getDuration(), false, Collections.<PromotionStatus>emptyList());
        }
        if (Result.UNSTABLE.equals(result)) {
            return StatusFactory.unstable(build.getTimeInMillis(), build.getDuration());
        }
        if (Result.NOT_BUILT.equals(result)) {
            return StatusFactory.notBuilt(build.getTimeInMillis(), build.getDuration());
        }
        throw new IllegalStateException("Result " + result + " not recognized.");
    }

    private static Status getStatusWithPromotionsFromSuccessResult(AbstractBuild build) {
        final Object action = build.getAction(PromotedBuildAction.class);
        if(action != null) {
            final List statusList = promotedBuildActionDecorator.getPromotions(action);
            if (CollectionUtils.isNotEmpty(statusList)) {
                final List<PromotionStatus> promotionStatusList = getPromotionStatuses(build, statusList);
                return StatusFactory.success(build.getTimeInMillis(), build.getDuration(), true, promotionStatusList);
            }
        }
        return StatusFactory.success(build.getTimeInMillis(), build.getDuration(), false, Collections.<PromotionStatus>emptyList());
    }

    private static List<PromotionStatus> getPromotionStatuses(AbstractBuild build, List statusList) {
        final List<PromotionStatus> promotionStatusList = new ArrayList<PromotionStatus>();
        for(Object status : statusList)
        {
            final List<String> params = new ArrayList<String>();
            if (!promotionStatusDecorator.getPromotionBuilds(status).isEmpty())
            {
                for(Promotion promotion : promotionStatusDecorator.getPromotionBuilds(status)) {
                    for (ParameterValue value : promotion.getParameterValues()) {
                        if (value instanceof StringParameterValue) {
                            if (StringUtils.isNotBlank(((StringParameterValue) value).value)) {
                                params.add("<strong>" + value.getName() + "</strong>: " + ((StringParameterValue) value).value);
                            }
                        } else if (value instanceof FileParameterValue) {
                            params.add("<strong>" + value.getName() + "</strong>: " + ((FileParameterValue) value).getLocation());
                        } else if (value instanceof BooleanParameterValue) {
                            if (((BooleanParameterValue) value).value) {
                                params.add("<strong>" + value.getName() + "</strong>: " + Boolean.toString(((BooleanParameterValue) value).value));
                            }
                        }
                        // TODO: there are more types
                    }
                    promotionStatusList.add(new PromotionStatus(promotionStatusDecorator.getName(status), promotion.getStartTimeInMillis(), promotion.getTime().getTime()-build.getTimeInMillis(),
                                        promotion.getUserName(), promotionStatusDecorator.getIcon(status, "16x16"), params));
                }
            }
        }
        sortPromotionStatusListByStartTimeInDescOrder(promotionStatusList);
        return promotionStatusList;
    }

    private static void sortPromotionStatusListByStartTimeInDescOrder(List promotionStatusList) {
        Collections.sort(promotionStatusList, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return (int) (promotionStatusDecorator.getStartTime(o2) - promotionStatusDecorator.getStartTime(o1));
            }
        });
    }


    @Override
    public String toString() {
        return String.valueOf(type);
    }

    /* package */ static void setPromotionStatusDecorator(PromotionStatusDecorator promotionStatusDecorator) {
        SimpleStatus.promotionStatusDecorator = promotionStatusDecorator;
    }

    /* package */ static void setPromotedBuildActionDecorator(PromotedBuildActionDecorator promotedBuildActionDecorator) {
        SimpleStatus.promotedBuildActionDecorator = promotedBuildActionDecorator;
    }
    // Decorator

    static public class PromotedBuildActionDecorator implements Action {
        public List getPromotions(Object action) {
            return ((PromotedBuildAction)action).getPromotions();
        }

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return null;
        }
    }

    static public class PromotionStatusDecorator {
        public Collection<Promotion> getPromotionBuilds(Object status) {
            return ((hudson.plugins.promoted_builds.Status)status).getPromotionBuilds();
        }

        public String getName(Object status) {
            return ((hudson.plugins.promoted_builds.Status)status).getName();
        }

        public String getIcon(Object status, String size) {
            return ((hudson.plugins.promoted_builds.Status)status).getIcon(size);
        }

        public long getStartTime(Object status) {
            return ((PromotionStatus)status).getStartTime();
        }
    }
}
