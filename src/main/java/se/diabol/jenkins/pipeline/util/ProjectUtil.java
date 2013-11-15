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
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.SubProjectsAction;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import se.diabol.jenkins.pipeline.PipelineProperty;

import java.util.*;

import static com.google.common.collect.Maps.newLinkedHashMap;


public abstract class ProjectUtil {

    public static ListBoxModel fillAllProjects(ItemGroup<?> context) {
        ListBoxModel options = new ListBoxModel();
        for (AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            options.add(p.getFullDisplayName(), p.getRelativeNameFrom(context));
        }
        return options;
    }

    public static Set<String> getStageNames() {
        List<AbstractProject> projects = Jenkins.getInstance().getAllItems(AbstractProject.class);
        Set<String> result = new HashSet<String>();
        for (AbstractProject project : projects) {
            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            if (property != null && property.getStageName() != null) {
                result.add(property.getStageName());
            }

        }
        return result;
    }

    public static Map<String, AbstractProject<?, ?>> getAllDownstreamProjects(AbstractProject first) {
        Map<String, AbstractProject<?, ?>> projects = newLinkedHashMap();
        projects.put(first.getName(), first);
        for (AbstractProject project : getDownstreamProjects(first))
            projects.putAll(getAllDownstreamProjects(project));
        return projects;
    }

    public static List<AbstractProject> getDownstreamProjects(AbstractProject<?, ?> project) {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        result.addAll(getSubProjects(project));
        result.addAll(project.getDownstreamProjects());
        return result;
    }

    protected static List<AbstractProject> getSubProjects(AbstractProject project) {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        for (SubProjectsAction action : Util.filter(project.getActions(), SubProjectsAction.class)) {
            for (BlockableBuildTriggerConfig config : action.getConfigs()) {
                for (AbstractProject subProject : config.getProjectList(project.getParent(), null)) {
                    result.add(subProject);
                }
            }
        }
        return result;
    }

    public static AbstractProject<?, ?> getProject(String name) {
        return Jenkins.getInstance().getItem(name, Jenkins.getInstance(), AbstractProject.class);
    }


}
