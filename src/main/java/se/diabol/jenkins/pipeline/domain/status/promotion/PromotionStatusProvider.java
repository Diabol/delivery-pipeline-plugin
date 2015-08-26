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
package se.diabol.jenkins.pipeline.domain.status.promotion;

import hudson.Extension;
import hudson.model.*;
import hudson.plugins.promoted_builds.Status;
import hudson.plugins.promoted_builds.PromotedBuildAction;
import hudson.plugins.promoted_builds.Promotion;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

@Extension(optional = true)
public class PromotionStatusProvider extends AbstractPromotionStatusProvider {

    // Force a classloading error plugin isn't available
    static final public Class<PromotedBuildAction> CLASS = PromotedBuildAction.class;
    static final String DEFAULT_ICON_SIZE = "16x16";

    private PromotionStatusWrapper promotionStatusWrapper = new PromotionStatusWrapper();
    private PromotedBuildActionWrapper promotedBuildActionWrapper = new PromotedBuildActionWrapper();

    // public

    public boolean isBuildPromoted(AbstractBuild<?, ?> build) {
        final Object action = build.getAction(PromotedBuildAction.class);
        if (action != null) {
            return (CollectionUtils.isNotEmpty(promotedBuildActionWrapper.getPromotions(action)));
        }
        return false;
    }

    public List<PromotionStatus> getPromotionStatusList(AbstractBuild<?, ?> build) {
        final List<PromotionStatus> promotionStatusList = new ArrayList<PromotionStatus>();
        final Object action = build.getAction(PromotedBuildAction.class);
        if (action != null) {
            for (Object status : promotedBuildActionWrapper.getPromotions(action)) {
                final List<String> params = new ArrayList<String>();
                for (Promotion promotion : (Collection<Promotion>) promotionStatusWrapper.getPromotionBuilds(status)) {
                    populatePromotionParameters(params, promotion);
                    promotionStatusList.add(buildNewPromotionStatus(build, status, params, promotion));
                }
            }
            sortPromotionStatusListByStartTimeInDescOrder(promotionStatusList);
        }
        return promotionStatusList;
    }

    // private

    private PromotionStatus buildNewPromotionStatus(AbstractBuild<?, ?> build, Object status, List<String> params, Object promotionObj) {
        final Promotion promotion = (Promotion) promotionObj;
        final String name = promotionStatusWrapper.getName(status);
        final long startTime = promotion.getStartTimeInMillis();
        final long duration = promotion.getTime().getTime() - build.getTimeInMillis();
        final String userName = promotion.getUserName();
        final String icon = promotionStatusWrapper.getIcon(status, DEFAULT_ICON_SIZE);

        return new PromotionStatus(name, startTime, duration, userName, icon, params);
    }

    private void populatePromotionParameters(List<String> params, Object promotionObj) {
        final Promotion promotion = (Promotion) promotionObj;
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
    }

    private void sortPromotionStatusListByStartTimeInDescOrder(List<PromotionStatus> promotionStatusList) {
        Collections.sort(promotionStatusList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return (int) (promotionStatusWrapper.getStartTime(o2) - promotionStatusWrapper.getStartTime(o1));
            }
        });
    }

    // Decorators to make code unit-testable

    static class PromotedBuildActionWrapper {
        public List<Status> getPromotions(Object action) {
            return ((PromotedBuildAction) action).getPromotions();
        }
    }

    static class PromotionStatusWrapper {
        public Collection<Promotion> getPromotionBuilds(Object status) {
            return ((hudson.plugins.promoted_builds.Status) status).getPromotionBuilds();
        }

        public String getName(Object status) {
            return ((hudson.plugins.promoted_builds.Status) status).getName();
        }

        public String getIcon(Object status, String size) {
            return ((hudson.plugins.promoted_builds.Status) status).getIcon(size);
        }

        public long getStartTime(Object status) {
            return ((PromotionStatus) status).getStartTime();
        }

        public long getDuration(Object status) {
            return ((PromotionStatus) status).getDuration();
        }
    }

    // package scope setters for unit testing

    void setPromotionStatusWrapper(PromotionStatusWrapper promotionStatusWrapper) {
        this.promotionStatusWrapper = promotionStatusWrapper;
    }

    void setPromotedBuildActionWrapper(PromotedBuildActionWrapper promotedBuildActionWrapper) {
        this.promotedBuildActionWrapper = promotedBuildActionWrapper;
    }
}
