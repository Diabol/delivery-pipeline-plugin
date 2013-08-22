package se.diabol.pipefitter;

import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.AbstractProject;
import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;
import se.diabol.pipefitter.model.status.StatusFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static se.diabol.pipefitter.model.status.StatusFactory.idle;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.singleton;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactory
{
    static List<AbstractProject> getAllDownstreamJobs(AbstractProject first) {
        List<AbstractProject> jobs = newArrayList();
        jobs.add(first);

        List<AbstractProject> downstreamProjects = first.getDownstreamProjects();
        for (AbstractProject project : downstreamProjects) {
            jobs.addAll(getAllDownstreamJobs(project));
        }

        return jobs;
    }

    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstJob)
    {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject job : getAllDownstreamJobs(firstJob))
        {
            PipelineProperty property = (PipelineProperty) job.getProperty(PipelineProperty.class);
            Task task = new Task(job.getName(), job.getDisplayName(), StatusFactory.idle()); // todo: Null not idle
            String stageName = property != null? property.getStageName(): job.getDisplayName();
            Stage stage = stages.get(stageName);
            if(stage == null)
                stage = new Stage(stageName, Collections.<Task>emptyList());
            stages.put(stageName,
                       new Stage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task)))));
        }

        return new Pipeline(name, newArrayList(stages.values()));
    }
}
