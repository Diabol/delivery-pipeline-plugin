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
package se.diabol.jenkins.pipeline.domain;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.*;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

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

    public static ManualStep resolveManualStep(AbstractProject project) {
        if (isManualTrigger(project)) {
            return new ManualStep(project.getName(), null, false, project.hasPermission(Item.BUILD), null);
        } else {
            return null;
        }
    }


    protected static boolean isManualTrigger(AbstractProject<?, ?> project) {
        List<AbstractProject> upstreamProjects = project.getUpstreamProjects();
        if (upstreamProjects.size() == 1) {
            AbstractProject<?,?> upstreamProject = upstreamProjects.get(0);
            DescribableList<Publisher, Descriptor<Publisher>> upstreamPublishersLists = upstreamProject.getPublishersList();
            for (Publisher upstreamPub : upstreamPublishersLists) {
                if (upstreamPub instanceof BuildPipelineTrigger) {
                    String names = ((BuildPipelineTrigger) upstreamPub).getDownstreamProjectNames();
                    if (ProjectUtil.getProjectList(names, project.getParent(), null).contains(project)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ManualStep getManualStepLatest(AbstractProject project, AbstractBuild build, AbstractBuild firstBuild) {
        if (isManualTrigger(project)) {
            AbstractProject<?, ?> upstream = (AbstractProject<?, ?>) project.getUpstreamProjects().get(0);
            AbstractBuild upstreamBuild = BuildUtil.match(upstream.getBuilds(), firstBuild);
            if (build == null) {
                if (upstreamBuild != null && !upstreamBuild.isBuilding() && !ProjectUtil.isQueued(project, firstBuild)) {
                    return new ManualStep(upstream.getRelativeNameFrom(Jenkins.getInstance()), String.valueOf(upstreamBuild.getNumber()), true, project.hasPermission(Item.BUILD), null);
                } else {
                    return new ManualStep(upstream.getRelativeNameFrom(Jenkins.getInstance()), null, false, project.hasPermission(Item.BUILD), null);
                }
            } else {
                //TODO get this from configuration of trigger?
                if (!build.isBuilding() && !ProjectUtil.isQueued(project, firstBuild) && build.getResult().isWorseThan(Result.UNSTABLE)) {
                    return new ManualStep(upstream.getRelativeNameFrom(Jenkins.getInstance()), String.valueOf(upstreamBuild.getNumber()), true, project.hasPermission(Item.BUILD), null);
                }
            }
        }
        return null;
    }

    public static ManualStep getManualStepAggregated(AbstractProject project, AbstractProject firstProject) {
        if (isManualTrigger(project)) {
            Map<String, String> versions = new HashMap<String, String>();
            AbstractProject<?, ?> upstream = (AbstractProject<?, ?>) project.getUpstreamProjects().get(0);
            for (AbstractBuild build: upstream.getBuilds()) {
                AbstractBuild versionBuild = BuildUtil.getFirstUpstreamBuild(build, firstProject);
                if (versionBuild != null) {
                    if (!versions.containsKey(versionBuild.getDisplayName())) {
                        versions.put(versionBuild.getDisplayName(), String.valueOf(versionBuild.getNumber()));
                    }
                }
            }
            if (versions.size() == 0) {
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
