package se.diabol.pipefitter;

import hudson.model.*;
import jenkins.model.Jenkins;
import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;
import se.diabol.pipefitter.model.status.Status;
import se.diabol.pipefitter.model.status.StatusFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static hudson.model.Result.*;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactory
{
    public Pipeline extractPipeline(String name, AbstractProject<?, ?> firstJob) {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject job : getAllDownstreamJobs(firstJob)) {
            PipelineProperty property = (PipelineProperty) job.getProperty(PipelineProperty.class);
            String taskName = property != null && !property.getTaskName().equals("") ? property.getTaskName() : job.getDisplayName();
            Task task = new Task(job.getName(), taskName, StatusFactory.idle()); // todo: Null not idle
            String stageName = property != null && !property.getStageName().equals("") ? property.getStageName() : job.getDisplayName();
            Stage stage = stages.get(stageName);
            if (stage == null)
                stage = new Stage(stageName, Collections.<Task>emptyList());
            stages.put(stageName,
                    new Stage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task)))));
        }

        return new Pipeline(name, newArrayList(stages.values()));
    }

    private List<AbstractProject> getAllDownstreamJobs(AbstractProject first)
    {
        List<AbstractProject> jobs = newArrayList();
        jobs.add(first);

        for (AbstractProject project : getDownstreamProjects(first)) {
            jobs.addAll(getAllDownstreamJobs(project));
        }

        return jobs;
    }

    /** Opens up for testing and mocking, since Jenkins has getDownstreamProjects() final */
    List<AbstractProject<?, ?>> getDownstreamProjects(AbstractProject project)
    {
        //noinspection unchecked
        return project.getDownstreamProjects();
    }


    public Pipeline createPipelineLatest(Pipeline pipeline) {
        AbstractBuild prevBuild = null;
        List<AbstractBuild> builds = new ArrayList<>();
        List<Stage> stages = new ArrayList<>();
        for (Stage stage : pipeline.getStages()) {
            List<Task> tasks = new ArrayList<>();
            for (Task task : stage.getTasks()) {
                AbstractProject job = Jenkins.getInstance().getItem(task.getId().toString(), Jenkins.getInstance(), AbstractProject.class);
                AbstractBuild build = job.getLastBuild();
                //Map<AbstractProject, Integer> hepp = build.getUpstreamBuilds();
                if (stages.isEmpty() || build != null && build.equals(getDownstreamBuild(job, prevBuild))) {
                    Status status = resolveStatus(build);
                    tasks.add(new Task(task.getId(), task.getName(), status));
                } else {
                    tasks.add(new Task(task.getId(), task.getName(), StatusFactory.idle()));
                }

                prevBuild = build;

            }
            stages.add(new Stage(stage.getName(), tasks));
        }
        return new Pipeline(pipeline.getName(), stages);

    }


    private static Status resolveStatus(AbstractBuild build)
    {
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

    public static AbstractBuild<?, ?> getDownstreamBuild(final AbstractProject<?, ?> downstreamProject,
                                                         final AbstractBuild<?, ?> upstreamBuild) {
        if ((downstreamProject != null) && (upstreamBuild != null)) {
            final List<AbstractBuild<?, ?>> downstreamBuilds = (List<AbstractBuild<?, ?>>) downstreamProject.getBuilds();
            for (final AbstractBuild<?, ?> innerBuild : downstreamBuilds) {
                for (final CauseAction action : innerBuild.getActions(CauseAction.class)) {
                    for (final Cause cause : action.getCauses()) {
                        if (cause instanceof Cause.UpstreamCause) {
                            final Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
                            if (upstreamCause.getUpstreamProject().equals(upstreamBuild.getProject().getFullName())
                                    && (upstreamCause.getUpstreamBuild() == upstreamBuild.getNumber())) {
                                return innerBuild;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

/*   public static boolean isDownstreamBuildFor(AbstractBuild firstBuild, AbstractBuild build) {
        List<AbstractProject> upStreamProjects = build.getProject().getUpstreamProjects();
        for (int i = 0; i < upStreamProjects.size(); i++) {
            AbstractProject project = upStreamProjects.get(i);
            List<CauseAction> actions = project.getLastBuild().getActions(CauseAction.class);
            for (int j = 0; j < actions.size(); j++) {
                CauseAction causeAction = actions.get(j);
                List<Cause> causes = causeAction.getCauses();
                for (int k = 0; k < causes.size(); k++) {
                    Cause cause = causes.get(k);
                    if (cause instanceof Cause.UpstreamCause) {
                        Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
                        if ()
                    }
                }
            }
        }
   }*/



//    }
}
