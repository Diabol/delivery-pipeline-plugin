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
package se.diabol.jenkins.pipeline.resolver;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.User;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import se.diabol.jenkins.pipeline.CauseResolver;
import se.diabol.jenkins.pipeline.domain.TriggerCause;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;

@Extension(ordinal = 1000)
public class CoreCauseResolver extends CauseResolver {

    @Override
    public TriggerCause resolveCause(Cause cause) {
        if (cause instanceof Cause.UserIdCause) {
            return new TriggerCause(TriggerCause.TYPE_MANUAL, "user "
                    + getDisplayName(((Cause.UserIdCause) cause).getUserName()));
        } else if (cause instanceof Cause.RemoteCause) {
            return new TriggerCause(TriggerCause.TYPE_REMOTE, "remote trigger");
        } else if (cause instanceof Cause.UpstreamCause) {
            Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
            AbstractProject upstreamProject = JenkinsUtil.getInstance().getItem(upstreamCause.getUpstreamProject(),
                    JenkinsUtil.getInstance(), AbstractProject.class);
            StringBuilder causeString = new StringBuilder("upstream project");
            if (upstreamProject != null) {

                causeString.append(" ").append(upstreamProject.getDisplayName());
                AbstractBuild upstreamBuild = upstreamProject.getBuildByNumber(upstreamCause.getUpstreamBuild());
                if (upstreamBuild != null) {
                    causeString.append(" build ").append(upstreamBuild.getDisplayName());
                }
            }
            return new TriggerCause(TriggerCause.TYPE_UPSTREAM, causeString.toString());
        } else if (cause instanceof Cause.UpstreamCause.DeeplyNestedUpstreamCause) {
            return new TriggerCause(TriggerCause.TYPE_UPSTREAM, "upstream");
        } else if (cause instanceof SCMTrigger.SCMTriggerCause) {
            return new TriggerCause(TriggerCause.TYPE_SCM, "SCM");
        } else if (cause instanceof TimerTrigger.TimerTriggerCause) {
            return new TriggerCause(TriggerCause.TYPE_TIMER, "timer");
        } else {
            return null;
        }
    }

    protected static String getDisplayName(String userName) {
        User user = JenkinsUtil.getInstance().getUser(userName);
        if (user != null) {
            return user.getDisplayName();
        } else {
            return "anonymous";
        }
    }

}
