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
package se.diabol.jenkins.pipeline.util;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.util.RunList;

import javax.annotation.CheckForNull;
import java.util.List;

public final class BuildUtil {

    private BuildUtil() {
    }

    @CheckForNull
    public static AbstractBuild getUpstreamBuild(AbstractBuild build) {
        List<CauseAction> actions = build.getActions(CauseAction.class);
        for (CauseAction action : actions) {
            List<Cause.UpstreamCause> causes = Util.filter(action.getCauses(), Cause.UpstreamCause.class);

            for (Cause.UpstreamCause upstreamCause : causes) {
                AbstractProject upstreamProject = JenkinsUtil.getInstance().getItemByFullName(upstreamCause.getUpstreamProject(), AbstractProject.class);
                //Due to https://issues.jenkins-ci.org/browse/JENKINS-14030 when a project has been renamed triggers are not updated correctly
                if (upstreamProject == null) {
                    return null;
                }
                return upstreamProject.getBuildByNumber(upstreamCause.getUpstreamBuild());
            }
        }
        return null;
    }

    /**
     * Finds the first upstream build in the chain of triggered builds.
     *
     * @param build the build to find the first upstream for
     * @return the first upstream build for the given build
     */
    @CheckForNull
    public static AbstractBuild getFirstUpstreamBuild(AbstractBuild build, AbstractProject first) {
        if (build == null) {
            return null;
        }
        if (build.getProject().equals(first)) {
            return build;
        }

        AbstractBuild upstreamBuild = BuildUtil.getUpstreamBuild(build);
        if (upstreamBuild != null) {
            if (upstreamBuild.getProject().equals(first)) {
                return upstreamBuild;
            } else {
                return getFirstUpstreamBuild(upstreamBuild, first);
            }
        }

        return build;
    }

    /**
     * Returns the build for a projects that has been triggered by the supplied upstream project.
     */
    @CheckForNull
    public static AbstractBuild match(RunList<? extends AbstractBuild> runList, AbstractBuild firstBuild) {
        if (firstBuild != null) {
            for (AbstractBuild currentBuild : runList) {
                AbstractBuild build = BuildUtil.getFirstUpstreamBuild(currentBuild, firstBuild.getProject());
                if (equals(build, firstBuild)) {
                    return currentBuild;
                }
            }
        }
        return null;
    }


    public static boolean equals(AbstractBuild b1, AbstractBuild b2) {
        return b1 != null && b2 != null && b1.getProject().getFullName().equals(b2.getProject().getFullName())
                && b1.getNumber() == b2.getNumber();

    }


}
