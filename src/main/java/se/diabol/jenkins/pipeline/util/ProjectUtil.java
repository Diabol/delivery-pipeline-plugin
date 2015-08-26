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
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.util.ListBoxModel;
import se.diabol.jenkins.pipeline.RelationshipResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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
        for (AbstractProject<?, ?> p : JenkinsUtil.getInstance().getAllItems(AbstractProject.class)) {
            options.add(p.getFullDisplayName(), p.getRelativeNameFrom(context));
        }
        return options;
    }

    /**
     * @see se.diabol.jenkins.pipeline.util.ProjectUtil#getAllDownstreamProjects(hudson.model.AbstractProject, java.util.Map)
     *
     */
    public static Map<String, AbstractProject<?, ?>> getAllDownstreamProjects(AbstractProject first, AbstractProject last) {
        Map<String, AbstractProject<?, ?>> projects = newLinkedHashMap();
        return  getAllDownstreamProjects(first, last, projects);
    }

    /**
     * Get all downstream projects for a given project. This will recursively call all downstream projects for a given first project.
     *
     * A project that has a downstream project and will eventually loop back to itself will log a warning, and will NOT add. Adding
     * a project that already exists will produce a stack overflow.
     *
     * @param first The first project
     * @param last The last project to visualize
     * @param projects Current map of all sub projects.
     * @return A map of all downstream projects.
     */
    public static Map<String, AbstractProject<?, ?>> getAllDownstreamProjects(AbstractProject first, AbstractProject last, Map<String, AbstractProject<?, ?>> projects) {
        if (first == null) {
            return projects;
        }

        if (projects.containsValue(first)) {
            return projects;
        }

        if (last != null && first.getFullName().equals(last.getFullName())) {
            projects.put(last.getFullName(), last);
            return projects;
        }

        projects.put(first.getFullName(), first);

        for (AbstractProject p : getDownstreamProjects(first)) {
            projects.putAll(getAllDownstreamProjects(p, last, projects));
        }

        return projects;
    }

    public static List<AbstractProject> getDownstreamProjects(AbstractProject<?, ?> project) {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        List<RelationshipResolver> resolvers = RelationshipResolver.all();
        for (RelationshipResolver resolver : resolvers) {
            result.addAll(resolver.getDownstreamProjects(project));
        }
        return result;
    }

    public static AbstractProject<?, ?> getProject(String name, ItemGroup context) {
        return JenkinsUtil.getInstance().getItem(name, context, AbstractProject.class);
    }

    public static Map<String, AbstractProject> getProjects(String regExp) {
        try {
            Pattern pattern = Pattern.compile(regExp);
            Map<String, AbstractProject> result = new HashMap<String, AbstractProject>();
            for (AbstractProject<?, ?> project : JenkinsUtil.getInstance().getAllItems(AbstractProject.class)) {
                Matcher matcher = pattern.matcher(project.getFullName());
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
                @SuppressWarnings("unchecked")
                List<AbstractProject<?,?>> upstreamProjects = project.getUpstreamProjects();
                for (AbstractProject<?, ?> upstreamProject : upstreamProjects) {
                    AbstractBuild upstreamBuild = BuildUtil.match(upstreamProject.getBuilds(), firstBuild);
                    if (upstreamBuild != null) {
                        for (Cause.UpstreamCause upstreamCause : causes) {
                            if (upstreamBuild.getNumber() == upstreamCause.getUpstreamBuild() && upstreamProject.getRelativeNameFrom(JenkinsUtil.getInstance()).equals(upstreamCause.getUpstreamProject())) {
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
