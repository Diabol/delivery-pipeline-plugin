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

import hudson.EnvVars;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.TopLevelItem;
import hudson.model.*;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import se.diabol.jenkins.pipeline.RelationshipResolver;

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
        if (first == null) {
            return projects;
        }
        projects.put(first.getName(), first);
        for (AbstractProject project : getDownstreamProjects(first)) {
            projects.putAll(getAllDownstreamProjects(project));
        }
        return projects;
    }

    public static List<AbstractProject> getDownstreamProjects(AbstractProject<?, ?> project) {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        List<RelationshipResolver> resolvers= RelationshipResolver.all();
        for (RelationshipResolver resolver : resolvers) {
            result.addAll(resolver.getDownstreamProjects(project));
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

    public static boolean isQueued(AbstractProject project, AbstractBuild firstBuild) {
        if (project.isInQueue()) {
            if (firstBuild == null) {
                return true;
            } else {
                List<Cause.UpstreamCause> causes = Util.filter(project.getQueueItem().getCauses(), Cause.UpstreamCause.class);
                List<AbstractProject<?,?>> upstreamProjects = project.getUpstreamProjects();
                for (AbstractProject<?, ?> upstreamProject : upstreamProjects) {
                    AbstractBuild upstreamBuild = BuildUtil.match(upstreamProject.getBuilds(), firstBuild);
                    if (upstreamBuild != null) {
                        for (Cause.UpstreamCause upstreamCause : causes) {
                            if (upstreamBuild.getNumber() == upstreamCause.getUpstreamBuild() && upstreamProject.getRelativeNameFrom(Jenkins.getInstance()).equals(upstreamCause.getUpstreamProject())) {
                                return true;
                            }

                        }
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static List<AbstractProject> getProjectList(String projects, ItemGroup context, EnvVars env) {
        List<AbstractProject> projectList = new ArrayList<AbstractProject>();

        // expand variables if applicable
        StringBuilder projectNames = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(projects, ",");
        while (tokens.hasMoreTokens()) {
            if (projectNames.length() > 0) {
                projectNames.append(',');
            }
            projectNames.append(env != null ? env.expand(tokens.nextToken().trim()) : tokens.nextToken().trim());
        }

        projectList.addAll(Items.fromNameList(context, projectNames.toString(), AbstractProject.class));
        return projectList;
    }


}
