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
package se.diabol.jenkins.pipeline.trigger;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;

@Extension(optional = true)
public class BPPManualTriggerResolver extends ManualTriggerResolver {

    // Force a classloading error plugin isn't available
    @SuppressWarnings("unused")
    public static final Class CLASS = BuildPipelineTrigger.class;


    @Override
    @CheckForNull
    public ManualTrigger getManualTrigger(AbstractProject<?, ?> project, AbstractProject<?, ?> downstream) {
        BuildPipelineTrigger bppTrigger = downstream.getPublishersList().get(BuildPipelineTrigger.class);
        if (bppTrigger != null) {
            String names = bppTrigger.getDownstreamProjectNames();
            if (ProjectUtil.getProjectList(names, project.getParent(), null).contains(project)) {
                return new BPPManualTrigger();
            }
        }
        return null;

    }

    public boolean isManualTrigger(AbstractProject<?, ?> project) {
        List<AbstractProject> upstreamProjects = project.getUpstreamProjects();
        if (upstreamProjects.size() > 0) {
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

    @Override
    public List<AbstractProject> getUpstreamManualTriggered(AbstractProject<?, ?> project) {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        List<AbstractProject> upstreamProjects = project.getUpstreamProjects();
        for (AbstractProject upstream : upstreamProjects) {
            @SuppressWarnings("unchecked")
            DescribableList<Publisher, Descriptor<Publisher>> upstreamPublishersLists = upstream.getPublishersList();
            for (Publisher upstreamPub : upstreamPublishersLists) {
                if (upstreamPub instanceof BuildPipelineTrigger) {
                    String names = ((BuildPipelineTrigger) upstreamPub).getDownstreamProjectNames();
                    if (ProjectUtil.getProjectList(names, project.getParent(), null).contains(project)) {
                        result.add(upstream);
                    }
                }
            }
        }
        return result;
    }
}
