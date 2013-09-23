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

import hudson.model.*;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;
import se.diabol.jenkins.pipeline.model.TestResult;
import se.diabol.jenkins.pipeline.model.status.Status;
import se.diabol.jenkins.pipeline.model.status.StatusFactory;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

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

    /**
     * Created a pipeline prototype for the supplied first project
     */
    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstProject) {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject project : getAllDownstreamProjects(firstProject).values()) {
            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            String taskName = property != null && !isNullOrEmpty(property.getTaskName())
                    ? property.getTaskName() : project.getDisplayName();
            Status status = project.isDisabled() ? disabled() : idle();
            //TODO add if manual triggered
            Task task = new Task(project.getName(), taskName, null, status, getJobUrl(project), false, null);
            String stageName = property != null && !isNullOrEmpty(property.getStageName())
                    ? property.getStageName() : project.getDisplayName();
            Stage stage = stages.get(stageName);
            if (stage == null)
                stage = new Stage(stageName, Collections.<Task>emptyList());
            stages.put(stageName,
                    new Stage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task)))));
        }

        return new Pipeline(name, null, null, null, newArrayList(stages.values()), false);
    }

    private static Map<String, AbstractProject> getAllDownstreamProjects(AbstractProject first) {
        Map<String, AbstractProject> projects = newLinkedHashMap();
        projects.put(first.getName(), first);
        for (AbstractProject project : getDownstreamProjects(first))
            projects.putAll(getAllDownstreamProjects(project));
        return projects;
    }

    /**
     * Opens up for testing and mocking, since Jenkins has getDownstreamProjects() final
     */
    static List<AbstractProject<?, ?>> getDownstreamProjects(AbstractProject project) {
        //noinspection unchecked
        return project.getDownstreamProjects();
    }

    /**
     * Opens up for testing and mocking, since Jenkins has getUrl() method final
     */
    static String getJobUrl(AbstractProject project) {
        return "job/" + project.getName();
    }

    /**
     * Helper method
     *
     * @see PipelineFactory#createPipelineLatest(se.diabol.jenkins.pipeline.model.Pipeline, int)
     */

    public static Pipeline createPipelineLatest(Pipeline pipeline) {
        List<Pipeline> pipelines = createPipelineLatest(pipeline, 1);
        return pipelines.size() > 0 ? pipelines.get(0) : null;
    }

    public static Pipeline createPipelineAggregated(Pipeline pipeline) {

        AbstractProject firstProject = getProject(pipeline.getStages().get(0).getTasks().get(0));
        List<Stage> stages = new ArrayList<>();
        for (Stage stage : pipeline.getStages()) {
            AbstractProject project = getProject(stage.getTasks().get(0));

            List<Task> tasks = new ArrayList<>();
            AbstractBuild versionBuild = getFirstUpstreamBuild(project, firstProject);
            String version = null;
            if (versionBuild != null) {
                version = versionBuild.getDisplayName();
            }
            for (Task task : stage.getTasks()) {
                AbstractProject taskProject = getProject(task);
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
        //TODO add triggeredBy
        return new Pipeline(pipeline.getName(), null, null, null, stages, true);
    }


    /**
     * Populates and return pipelines for the supplied pipeline prototype with the current status.
     *
     * @param pipeline      the pipeline prototype
     * @param noOfPipelines number of pipeline instances
     */
    public static List<Pipeline> createPipelineLatest(Pipeline pipeline, int noOfPipelines) {
        Task firstTask = pipeline.getStages().get(0).getTasks().get(0);
        AbstractProject firstProject = getProject(firstTask);

        List<Pipeline> result = new ArrayList<>();

        Iterator it = firstProject.getBuilds().iterator();
        for (int i = 0; i < noOfPipelines && it.hasNext(); i++) {
            AbstractBuild firstBuild = (AbstractBuild) it.next();
            String timestamp = PipelineUtils.formatTimestamp(firstBuild.getTimeInMillis());
            List<Stage> stages = new ArrayList<>();
            for (Stage stage : pipeline.getStages()) {
                List<Task> tasks = new ArrayList<>();
                for (Task task : stage.getTasks()) {
                    AbstractProject taskProject = getProject(task);
                    AbstractBuild currentBuild = match(taskProject.getBuilds(), firstBuild);
                    Status status = resolveStatus(taskProject, currentBuild);
                    String link = status.isIdle() || status.isQueued() ? task.getLink() : currentBuild.getUrl();
                    String buildId = status.isIdle() || status.isQueued() ? null : String.valueOf(currentBuild.getNumber());
                    tasks.add(new Task(task.getId(), task.getName(), buildId, status, link, task.isManual(), getTestResult(currentBuild)));
                }
                stages.add(new Stage(stage.getName(), tasks));
            }

            result.add(new Pipeline(pipeline.getName(), firstBuild.getDisplayName(), timestamp, getTriggeredBy(firstBuild), stages, false));
        }
        return result;
    }

    private static TestResult getTestResult(AbstractBuild build) {
        if (build != null) {
            AggregatedTestResultAction tests = build.getAction(AggregatedTestResultAction.class);
            if (tests != null) {
                return new TestResult(tests.getFailCount(), tests.getSkipCount(), tests.getTotalCount(),
                        Jenkins.getInstance().getRootUrl() + build.getUrl() + tests.getUrlName());
            }
        }
        return null;
    }

    private static String getTriggeredBy(AbstractBuild build) {
        Set<User> users = build.getCulprits();
        List<String> triggeredBy = new ArrayList<>();

        for (User user : users) {
            triggeredBy.add(user.getDisplayName());
        }

        if (triggeredBy.size() > 0) {
            return StringUtils.join(triggeredBy.toArray(), ", ");
        }

        Cause.UserIdCause cause = (Cause.UserIdCause) build.getCause(Cause.UserIdCause.class);
        if (cause != null && cause.getUserName() != null) {
            return cause.getUserName();
        } else {
            return "anonymous";
        }

    }

    /**
     * Returns the build for a projects that has been triggered by the supplied upstream project.
     */
    private static AbstractBuild match(RunList runList, AbstractBuild firstBuild) {
        if (firstBuild != null) {
            for (Object aRunList : runList) {
                AbstractBuild currentBuild = (AbstractBuild) aRunList;
                if (firstBuild.equals(getFirstUpstreamBuild(currentBuild))) {
                    return currentBuild;
                }
            }
        }
        return null;
    }

    private static AbstractProject getProject(Task task) {
        return Jenkins.getInstance().getItem(task.getId(), Jenkins.getInstance().getItemGroup(), AbstractProject.class);
    }

    protected static Status resolveStatus(AbstractProject project, AbstractBuild build) {
        if (build == null) {
            if (project.isInQueue())
                return StatusFactory.queued(project.getQueueItem().getInQueueSince());
            else if (project.isDisabled())
                return StatusFactory.disabled();
            else
                return StatusFactory.idle();
        }

        if (build.isBuilding()) {
            return StatusFactory.running((int) round(100.0d * (currentTimeMillis() - build.getTimestamp().getTimeInMillis())
                    / build.getEstimatedDuration()), build.getTimeInMillis(), currentTimeMillis() - build.getTimestamp().getTimeInMillis());
        }

        Result result = build.getResult();
        if (ABORTED.equals(result))
            return StatusFactory.cancelled(build.getTimeInMillis(), build.getDuration());
        else if (SUCCESS.equals(result))
            return StatusFactory.success(build.getTimeInMillis(), build.getDuration());
        else if (FAILURE.equals(result))
            return StatusFactory.failed(build.getTimeInMillis(), build.getDuration());
        else if (UNSTABLE.equals(result))
            return StatusFactory.unstable(build.getTimeInMillis(), build.getDuration());
        else
            throw new IllegalStateException("Result " + result + " not recognized.");
    }

    /**
     * Finds the first upstream build in the chain of triggered builds.
     *
     * @param build the build to find the first upstream for
     * @return the first upstream build for the given build
     */
    private static AbstractBuild getFirstUpstreamBuild(AbstractBuild build) {
        if (build == null) {
            return null;
        }
        AbstractBuild upstreamBuild = getUpstreamBuild(build);
        if (upstreamBuild != null) {
            return getFirstUpstreamBuild(upstreamBuild);
        }

        return build;
    }


    private static AbstractBuild getFirstUpstreamBuild(AbstractProject project, AbstractProject first) {
        RunList builds = project.getBuilds();
        for (Object build1 : builds) {
            AbstractBuild b = (AbstractBuild) build1;
            AbstractBuild upstream = getFirstUpstreamBuild(b);
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