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
import hudson.model.TopLevelItem;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.SubProjectsAction;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.google.common.collect.Maps.newLinkedHashMap;


public final class ProjectUtil {

    private static final Logger LOG = Logger.getLogger(ProjectUtil.class.getName());

    private ProjectUtil() {
    }

    public static ListBoxModel fillAllProjects(ItemGroup<?> context) {
        ListBoxModel options = new ListBoxModel();
        for (AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            options.add(p.getFullDisplayName(), p.getRelativeNameFrom(context));
        }
        return options;
    }

    public static Map<String, AbstractProject<?, ?>> getAllDownstreamProjects(AbstractProject first) {
        Map<String, AbstractProject<?, ?>> projects = newLinkedHashMap();
        projects.put(first.getName(), first);
        for (AbstractProject project : getDownstreamProjects(first)) {
            projects.putAll(getAllDownstreamProjects(project));
        }
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

    public static AbstractProject<?, ?> getProject(String name, ItemGroup context) {
        return Jenkins.getInstance().getItem(name, context, AbstractProject.class);
    }

    public static AbstractProject getProject(String name) {
        Map<String, TopLevelItem> items = Jenkins.getInstance().getItemMap();
        if (items.containsKey(name)) {
            return (AbstractProject) items.get(name);
        } else {
            List<ItemGroup> groups = Util.createSubList(items.values(), ItemGroup.class);
            for (ItemGroup group : groups) {
                AbstractProject project = find(group, name);
                if (project != null) {
                    return project;
                }
            }

        }
        return null;
    }


    private static AbstractProject find(ItemGroup group, String name) {

        List<AbstractProject> projects = Util.createSubList(group.getItems(), AbstractProject.class);
        for (AbstractProject project : projects) {
            if (project.getRelativeNameFrom(Jenkins.getInstance()).equals(name)) {
                return project;
            }
        }

        List<ItemGroup> groups = Util.createSubList(group.getItems(), ItemGroup.class);
        for (ItemGroup itemGroup : groups) {
            AbstractProject project = find(itemGroup, name);
            if (project != null) {
                return project;
            }
        }
        return null;
    }


    public static Map<String, AbstractProject> getProjects(String regExp) {
        try {
            Pattern pattern = Pattern.compile(regExp);
            Map<String, AbstractProject> result = new HashMap<String, AbstractProject>();
            for (AbstractProject<?, ?> project : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                Matcher matcher = pattern.matcher(project.getName());
                if (matcher.find()) {
                    if (matcher.groupCount() >= 1) {
                        String name = matcher.group(1);
                        result.put(name, project);
                    } else {
                        LOG.log(Level.WARNING, "Could not find match group");
                    }
                }
            }
            return result;
        } catch (PatternSyntaxException e) {
            LOG.log(Level.WARNING, "Could not find projects on regular expression", e);
            return Collections.emptyMap();
        }
    }

}
