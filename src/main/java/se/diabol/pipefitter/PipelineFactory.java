package se.diabol.pipefitter;

import hudson.model.*;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;
import se.diabol.pipefitter.model.status.Status;
import se.diabol.pipefitter.model.status.StatusFactory;

import java.util.*;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static hudson.model.Result.*;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;
import static se.diabol.pipefitter.model.status.StatusFactory.idle;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactory {
    private static final Jenkins JENKINS = Jenkins.getInstance();

    public Pipeline extractPipeline(String name, AbstractProject<?, ?> firstJob) {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject job : getAllDownstreamJobs(firstJob).values()) {
            PipelineProperty property = (PipelineProperty) job.getProperty(PipelineProperty.class);
            String taskName = property != null && !property.getTaskName().equals("") ? property.getTaskName() : job.getDisplayName();
            Task task = new Task(job.getName(), taskName, idle(), getUrl(job)); // todo: Null not idle
            String stageName = property != null && !property.getStageName().equals("") ? property.getStageName() : job.getDisplayName();
            Stage stage = stages.get(stageName);
            if (stage == null)
                stage = new Stage(stageName, Collections.<Task>emptyList());
            stages.put(stageName,
                    new Stage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task)))));
        }

        return new Pipeline(name, null, newArrayList(stages.values()));
    }

    private Map<String, AbstractProject> getAllDownstreamJobs(AbstractProject first) {
        Map<String, AbstractProject> jobs = newLinkedHashMap();
        jobs.put(first.getName(), first);
        for (AbstractProject project : getDownstreamProjects(first))
            jobs.putAll(getAllDownstreamJobs(project));
        return jobs;
    }

    /**
     * Opens up for testing and mocking, since Jenkins has getDownstreamProjects() final
     */
    List<AbstractProject<?, ?>> getDownstreamProjects(AbstractProject project) {
        //noinspection unchecked
        return project.getDownstreamProjects();
    }

    /**
     * Opens up for testing and mocking, since Jenkins has getUrl() method final
     * @param job
     * @return
     */
    String getUrl(AbstractProject job)
    {
        return job.getUrl();
    }


    public Pipeline createPipelineLatest(Pipeline pipeline) {
        List<Pipeline> pipelines = createPipelineLatest(pipeline, 1);
        return pipelines.size() > 0 ? pipelines.get(0) : null;
    }

    public List<Pipeline> createPipelineLatest(Pipeline pipeline, int noOfPipelines) {
        Task firstTask = pipeline.getStages().get(0).getTasks().get(0);
        AbstractProject firstProject = getJenkinsJob(firstTask);

        List<Pipeline> result = new ArrayList<>();

        Iterator it = firstProject.getBuilds().iterator();
        for (int i = 0; i < noOfPipelines && it.hasNext(); i++) {
            AbstractBuild lastBuild = (AbstractBuild) it.next();
            List<Stage> stages = new ArrayList<>();
            for (Stage stage : pipeline.getStages()) {
                List<Task> tasks = new ArrayList<>();
                for (Task task : stage.getTasks()) {
                    AbstractProject job = getJenkinsJob(task);
                    AbstractBuild currentBuild = match(job.getBuilds(), lastBuild);

                    Status status = currentBuild != null ? resolveStatus(currentBuild) : idle();
                    tasks.add(new Task(task.getId(), task.getName(), status, task.getLink()));
                }
                stages.add(new Stage(stage.getName(), tasks));
            }
            result.add(new Pipeline(pipeline.getName(), lastBuild.getDisplayName(), stages));


        }
        return result;
    }

    private AbstractBuild match(RunList runList, AbstractBuild firstJob) {
        Iterator it = runList.iterator();
        while (it.hasNext()) {
            AbstractBuild currentBuild = (AbstractBuild) it.next();
            if (firstJob.equals(getFirstUpstreamBuild(currentBuild))) {
                return currentBuild;
            }
        }
        return null;
    }

    private AbstractProject getJenkinsJob(Task task) {
        return JENKINS.getItem(task.getId().toString(), JENKINS, AbstractProject.class);
    }

    private Status resolveStatus(AbstractBuild build) {
        if (build.isBuilding()) {
            return StatusFactory.running((int) round(100.0d * (currentTimeMillis() - build.getTimestamp().getTimeInMillis())
                    / build.getEstimatedDuration()));
        }

        Result result = build.getResult();
        if (ABORTED.equals(result))
            return StatusFactory.cancelled();
        else if (SUCCESS.equals(result))
            return StatusFactory.success();
        else if (FAILURE.equals(result))
            return StatusFactory.failed();
        else if (UNSTABLE.equals(result))
            return StatusFactory.unstable();
        throw new IllegalStateException("Result " + result + " not recognized.");
    }

    /**
     * Finds the first upstream job in the chain of triggered jobs.
     *
     * @param build the build to find the first upstream for
     * @return the first upstream build for the given build
     */
    private static AbstractBuild getFirstUpstreamBuild(AbstractBuild build) {
        //build.getCause do not return the correct Causes sometimes
        List<CauseAction> actions = build.getActions(CauseAction.class);
        for (CauseAction action : actions) {
            List<Cause> causes = action.getCauses();
            for (Cause cause : causes) {
                if (cause instanceof Cause.UpstreamCause) {
                    Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
                    AbstractProject upstreamProject = Jenkins.getInstance().getItem(upstreamCause.getUpstreamProject(), Jenkins.getInstance(), AbstractProject.class);
                    AbstractBuild upstreamBuild = upstreamProject.getBuildByNumber(upstreamCause.getUpstreamBuild());
                    return getFirstUpstreamBuild(upstreamBuild);
                }
            }
        }
        return build;
    }

}