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
package se.diabol.jenkins.pipeline.domain.task;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Result;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.trigger.ManualTriggerResolver;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class ManualStep {
    private String upstreamProject;
    private String upstreamId;
    private boolean enabled;
    private boolean permission;
    private Map<String, String> possibleVersions;

    public ManualStep(String upstreamProject, String upstreamId, boolean enabled, boolean permission,
                      Map<String, String> possibleVersions) {
        this.upstreamProject = upstreamProject;
        this.upstreamId = upstreamId;
        this.enabled = enabled;
        this.permission = permission;
        this.possibleVersions = possibleVersions;
    }

    @CheckForNull
    public static ManualStep resolveManualStep(AbstractProject project) {
        if (isManualTrigger(project)) {
            return new ManualStep(project.getName(), null, false, project.hasPermission(Item.BUILD), null);
        } else {
            return null;
        }
    }


    protected static boolean isManualTrigger(AbstractProject<?, ?> project) {
        List<ManualTriggerResolver> resolvers = ManualTriggerResolver.all();
        for (ManualTriggerResolver manualTriggerResolver : resolvers) {
            if (manualTriggerResolver.isManualTrigger(project)) {
                return true;
            }
        }
        return false;
    }

    protected static List<AbstractProject> getUpstreamManualTriggered(AbstractProject<?, ?> project) {
        List<ManualTriggerResolver> resolvers = ManualTriggerResolver.all();
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        for (ManualTriggerResolver manualTriggerResolver : resolvers) {
            result.addAll(manualTriggerResolver.getUpstreamManualTriggered(project));
        }
        return result;
    }

    @CheckForNull
    public static ManualStep getManualStepLatest(AbstractProject project, AbstractBuild build, AbstractBuild firstBuild) {
        if (isManualTrigger(project)) {

            List<AbstractProject> upstreams = getUpstreamManualTriggered(project);
            for (int i = 0; i < upstreams.size(); i++) {
                AbstractProject upstream = upstreams.get(i);
                @SuppressWarnings("unchecked")
                AbstractBuild upstreamBuild = BuildUtil.match(upstream.getBuilds(), firstBuild);
                if (build == null) {
                    if (upstreamBuild != null && !upstreamBuild.isBuilding() && !ProjectUtil.isQueued(project, firstBuild)) {
                        Result result = upstreamBuild.getResult();
                        return new ManualStep(upstream.getRelativeNameFrom(JenkinsUtil.getInstance()), String.valueOf(upstreamBuild.getNumber()), result != null && !result.isWorseThan(Result.UNSTABLE), project.hasPermission(Item.BUILD), null);
                    }
                } else {
                    Result result = build.getResult();
                    if (upstreamBuild != null && !build.isBuilding() && !ProjectUtil.isQueued(project, firstBuild) && result != null && result.isWorseThan(Result.UNSTABLE)) {
                        return new ManualStep(upstream.getRelativeNameFrom(JenkinsUtil.getInstance()), String.valueOf(upstreamBuild.getNumber()), true, project.hasPermission(Item.BUILD), null);
                    }
                }
                if (i == upstreams.size() - 1) {
                    return new ManualStep(upstream.getRelativeNameFrom(JenkinsUtil.getInstance()), null, false, project.hasPermission(Item.BUILD), null);
                }
            }
        }
        return null;
    }

    @CheckForNull
    public static ManualStep getManualStepAggregated(AbstractProject project, AbstractProject firstProject) {
        if (isManualTrigger(project)) {
            Map<String, String> versions = new HashMap<String, String>();
            AbstractProject<?, ?> upstream = (AbstractProject<?, ?>) project.getUpstreamProjects().get(0);
            for (AbstractBuild build : upstream.getBuilds()) {
                AbstractBuild versionBuild = BuildUtil.getFirstUpstreamBuild(build, firstProject);
                if (versionBuild != null && !versions.containsKey(versionBuild.getDisplayName())) {
                    versions.put(versionBuild.getDisplayName(), String.valueOf(versionBuild.getNumber()));
                }
            }
            if (versions.isEmpty()) {
                return new ManualStep(upstream.getName(), null, false, project.hasPermission(Item.BUILD), versions);
            }
            return new ManualStep(upstream.getName(), null, true, project.hasPermission(Item.BUILD), versions);
        }
        return null;
    }

    @Exported
    public String getUpstreamProject() {
        return upstreamProject;
    }

    @Exported
    public String getUpstreamId() {
        return upstreamId;
    }

    @Exported
    public boolean isEnabled() {
        return enabled;
    }

    @Exported
    public boolean isPermission() {
        return permission;
    }

    @Exported
    public Map<String, String> getPossibleVersions() {
        return possibleVersions;
    }
}
