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
package se.diabol.jenkins.pipeline;

import hudson.ExtensionList;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import hudson.tasks.UserAvatarResolver;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import se.diabol.jenkins.pipeline.model.*;
import se.diabol.jenkins.pipeline.model.status.Status;
import se.diabol.jenkins.pipeline.model.status.StatusFactory;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static hudson.model.Result.*;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;
import static se.diabol.jenkins.pipeline.model.status.StatusFactory.disabled;
import static se.diabol.jenkins.pipeline.model.status.StatusFactory.idle;

public abstract class PipelineFactory {

    private static final int AVATAR_SIZE = 16;

    /**
     * Created a pipeline prototype for the supplied first project
     */
    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstProject) {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject project : ProjectUtil.getAllDownstreamProjects(firstProject).values()) {
            Task task = getPrototypeTask(project);
            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            String stageName = property != null && !isNullOrEmpty(property.getStageName())
                    ? property.getStageName() : project.getDisplayName();
            Stage stage = stages.get(stageName);
            if (stage == null) {
                stage = new Stage(stageName, Collections.<Task>emptyList());
            }
            stages.put(stageName,
                    new Stage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task)))));
        }

        return new Pipeline(name, null, null, null, null, null, newArrayList(stages.values()), false);
    }

    private static Task getPrototypeTask(AbstractProject project) {
        PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
        String taskName = property != null && !isNullOrEmpty(property.getTaskName())
                ? property.getTaskName() : project.getDisplayName();
        Status status = project.isDisabled() ? disabled() : idle();
        return new Task(project.getRelativeNameFrom(Jenkins.getInstance()), taskName, null, status, project.getUrl(), false, null);
    }

    /**
     * Helper method
     *
     * @see PipelineFactory#createPipelineLatest(se.diabol.jenkins.pipeline.model.Pipeline, int, ItemGroup)
     */

    public static Pipeline createPipelineLatest(Pipeline pipeline, ItemGroup context) {
        List<Pipeline> pipelines = createPipelineLatest(pipeline, 1, context);
        return pipelines.size() > 0 ? pipelines.get(0) : null;
    }

    public static Pipeline createPipelineAggregated(Pipeline pipeline, ItemGroup context) {

        AbstractProject firstProject = getProject(pipeline.getStages().get(0).getTasks().get(0), context);
        List<Stage> stages = new ArrayList<Stage>();
        for (Stage stage : pipeline.getStages()) {

            List<Task> tasks = new ArrayList<Task>();

            //The version build for this stage is the highest first task build
            AbstractBuild versionBuild = getHighestBuild(stage.getTasks(), firstProject, context);

            String version = null;
            if (versionBuild != null) {
                version = versionBuild.getDisplayName();
            }
            for (Task task : stage.getTasks()) {
                AbstractProject<?, ?> taskProject = getProject(task, context);
                AbstractBuild currentBuild = match(taskProject.getBuilds(), versionBuild);

                if (currentBuild != null) {
                    Status status = resolveStatus(taskProject, currentBuild);
                    String link = status.isIdle() ? task.getLink() : currentBuild.getUrl();
                    tasks.add(new Task(task.getId(), task.getName(), String.valueOf(currentBuild.getNumber()), status, link, task.isManual(), getTestResult(currentBuild)));
                } else {
                    tasks.add(new Task(task.getId(), task.getName(), null, StatusFactory.idle(), task.getLink(), task.isManual(), null));
                }
            }
            stages.add(new Stage(stage.getName(), tasks, version));
        }
        return new Pipeline(pipeline.getName(), null, null, null, null,null, stages, true);
    }

    private static AbstractBuild getHighestBuild(List<Task> tasks, AbstractProject firstProject, ItemGroup context) {
        int highest = -1;
        for (Task task : tasks) {
            AbstractProject project = getProject(task, context);
            AbstractBuild firstBuild = getFirstUpstreamBuild(project, firstProject);
            if (firstBuild != null && firstBuild.getNumber() > highest) {
                highest = firstBuild.getNumber();
            }
        }

        if (highest > 0) {
            return firstProject.getBuildByNumber(highest);
        } else {
            return null;
        }
    }


    /**
     * Populates and return pipelines for the supplied pipeline prototype with the current status.
     *
     * @param pipeline      the pipeline prototype
     * @param noOfPipelines number of pipeline instances
     */
    public static List<Pipeline> createPipelineLatest(Pipeline pipeline, int noOfPipelines, ItemGroup context) {
        Task firstTask = pipeline.getStages().get(0).getTasks().get(0);
        AbstractProject firstProject = getProject(firstTask, context);

        List<Pipeline> result = new ArrayList<Pipeline>();

        Iterator it = firstProject.getBuilds().iterator();
        for (int i = 0; i < noOfPipelines && it.hasNext(); i++) {
            AbstractBuild firstBuild = (AbstractBuild) it.next();
            List<Change> changes = getChanges(firstBuild);
            String timestamp = PipelineUtils.formatTimestamp(firstBuild.getTimeInMillis());
            List<Stage> stages = new ArrayList<Stage>();
            for (Stage stage : pipeline.getStages()) {
                List<Task> tasks = new ArrayList<Task>();
                for (Task task : stage.getTasks()) {
                    AbstractProject<?, ?> taskProject = getProject(task, context);
                    AbstractBuild currentBuild = match(taskProject.getBuilds(), firstBuild);
                    tasks.add(getTask(task, currentBuild, context));
                }
                stages.add(new Stage(stage.getName(), tasks));
            }

            result.add(new Pipeline(pipeline.getName(), firstBuild.getDisplayName(), changes, timestamp,
                    getTriggeredBy(firstBuild), getContributors(firstBuild), stages, false));
        }
        return result;
    }

    protected static List<Change> getChanges(AbstractBuild<?, ?> build) {
        RepositoryBrowser repositoryBrowser = build.getProject().getScm().getBrowser();
        List<Change> result = new ArrayList<Change>();
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            UserInfo user = getUser(entry.getAuthor());
            String changeLink = null;
            if (repositoryBrowser != null) {
                try {
                    URL link = repositoryBrowser.getChangeSetLink(entry);
                    if (link != null) {
                        changeLink = link.toExternalForm();
                    }
                } catch (IOException e) {
                    //Ignore
                }
            }
            result.add(new Change(user, entry.getMsg(), entry.getCommitId(), changeLink));
        }
        return result;
    }


    private static Task getTask(Task task, AbstractBuild build, ItemGroup context) {
        AbstractProject project = getProject(task, context);
        Status status = resolveStatus(project, build);
        String link = build == null || status.isIdle() || status.isQueued() ? task.getLink() : build.getUrl();
        String buildId = build == null || status.isIdle() || status.isQueued() ? null : String.valueOf(build.getNumber());
        return new Task(task.getId(), task.getName(), buildId, status, link, task.isManual(), getTestResult(build));
    }


    protected static TestResult getTestResult(AbstractBuild build) {
        if (build != null) {
            AggregatedTestResultAction tests = build.getAction(AggregatedTestResultAction.class);
            if (tests != null) {
                return new TestResult(tests.getFailCount(), tests.getSkipCount(), tests.getTotalCount(),
                        build.getUrl() + tests.getUrlName());
            }
        }
        return null;
    }

    protected static Set<UserInfo> getContributors(AbstractBuild<?, ?> build) {
        Set<UserInfo> contributors = new HashSet<UserInfo>();
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            contributors.add(getUser(entry.getAuthor()));
        }
        return contributors;
    }

    protected static List<Trigger> getTriggeredBy(AbstractBuild<?, ?> build) {
        List<Trigger> result = new ArrayList<Trigger>();
        List<Cause> causes = build.getCauses();
        for (Cause cause : causes) {
           if(cause instanceof Cause.UserIdCause){
               result.add(new Trigger("MANUAL", "user " + getDisplayName(((Cause.UserIdCause) cause).getUserName())));
           } else if(cause instanceof Cause.RemoteCause){
               result.add(new Trigger("REMOTE", "remote trigger"));
           } else if(cause instanceof Cause.UpstreamCause){
               //TODO add which project!
               result.add(new Trigger("UPSTREAM", "upstream"));
           } else if(cause instanceof SCMTrigger.SCMTriggerCause){
               result.add(new Trigger("SCM", "SCM change"));
           } else if(cause instanceof TimerTrigger.TimerTriggerCause){
               result.add(new Trigger("TIMER", "timer"));
           } else if(cause instanceof Cause.UpstreamCause.DeeplyNestedUpstreamCause){
               //TODO add which project!
               result.add(new Trigger("UPSTREAM", "upstream"));
           } else {
               result.add(new Trigger("UNKNOWN", "unknown cause"));
           }
        }
        return result;
    }

    private static String getDisplayName(String userName) {
        return Jenkins.getInstance().getUser(userName).getDisplayName();
    }

    private static UserInfo getUser(User user) {
        return new UserInfo(user.getDisplayName(), user.getUrl(), getAvatarUrl(user));
    }

    private static String getAvatarUrl(User user) {
        ExtensionList<UserAvatarResolver> resolvers = UserAvatarResolver.all();
        for (UserAvatarResolver resolver : resolvers) {
            String avatarUrl = resolver.findAvatarFor(user, AVATAR_SIZE, AVATAR_SIZE);
            if (avatarUrl != null) {
                return avatarUrl;
            }
        }
        return null;
    }


    /**
     * Returns the build for a projects that has been triggered by the supplied upstream project.
     */
    private static AbstractBuild match(RunList<? extends AbstractBuild> runList, AbstractBuild firstBuild) {
        if (firstBuild != null) {
            for (AbstractBuild currentBuild : runList) {
                if (firstBuild.equals(getFirstUpstreamBuild(currentBuild, firstBuild.getProject()))) {
                    return currentBuild;
                }
            }
        }
        return null;
    }

    private static AbstractProject getProject(Task task, ItemGroup context) {
        return ProjectUtil.getProject(task.getId(), context);
    }

    protected static Status resolveStatus(AbstractProject project, AbstractBuild build) {
        if (build == null) {
            if (project.isInQueue()) {
                return StatusFactory.queued(project.getQueueItem().getInQueueSince());
            } else if (project.isDisabled()) {
                return StatusFactory.disabled();
            } else {
                return StatusFactory.idle();
            }
        }

        if (build.isBuilding()) {
            return StatusFactory.running((int) round(100.0d * (currentTimeMillis() - build.getTimestamp().getTimeInMillis())
                    / build.getEstimatedDuration()), build.getTimeInMillis(), currentTimeMillis() - build.getTimestamp().getTimeInMillis());
        }

        Result result = build.getResult();
        if (ABORTED.equals(result)) {
            return StatusFactory.cancelled(build.getTimeInMillis(), build.getDuration());
        }
        if (SUCCESS.equals(result)) {
            return StatusFactory.success(build.getTimeInMillis(), build.getDuration());
        }
        if (FAILURE.equals(result)) {
            return StatusFactory.failed(build.getTimeInMillis(), build.getDuration());
        }
        if (UNSTABLE.equals(result)) {
            return StatusFactory.unstable(build.getTimeInMillis(), build.getDuration());
        } else {
            throw new IllegalStateException("Result " + result + " not recognized.");
        }
    }

    /**
     * Finds the first upstream build in the chain of triggered builds.
     *
     * @param build the build to find the first upstream for
     * @return the first upstream build for the given build
     */
    protected static AbstractBuild getFirstUpstreamBuild(AbstractBuild build, AbstractProject first) {
        if (build == null) {
            return null;
        }
        if (build.getProject().equals(first)) {
            return build;
        }

        AbstractBuild upstreamBuild = getUpstreamBuild(build);
        if (upstreamBuild != null) {
            if (upstreamBuild.getProject().equals(first)) {
                return upstreamBuild;
            } else {
                return getFirstUpstreamBuild(upstreamBuild, first);
            }
        }

        return build;
    }


    private static AbstractBuild getFirstUpstreamBuild(AbstractProject<?, ?> project, AbstractProject<?, ?> first) {
        RunList<? extends AbstractBuild> builds = project.getBuilds();
        for (AbstractBuild build : builds) {
            AbstractBuild upstream = getFirstUpstreamBuild(build, first);
            if (upstream != null && upstream.getProject().equals(first)) {
                return upstream;
            }
        }
        return null;
    }


    public static AbstractBuild getUpstreamBuild(AbstractBuild build) {
        List<CauseAction> actions = build.getActions(CauseAction.class);
        for (CauseAction action : actions) {
            List<Cause> causes = action.getCauses();
            for (Cause cause : causes) {
                if (cause instanceof Cause.UpstreamCause) {
                    Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
                    AbstractProject upstreamProject = Jenkins.getInstance().getItem(upstreamCause.getUpstreamProject(), Jenkins.getInstance(), AbstractProject.class);
                    //Due to https://issues.jenkins-ci.org/browse/JENKINS-14030 when a project has been renamed triggers are not updated correctly
                    if (upstreamProject == null) {
                        return null;
                    }
                    return upstreamProject.getBuildByNumber(upstreamCause.getUpstreamBuild());
                }
            }
        }
        return null;

    }


}