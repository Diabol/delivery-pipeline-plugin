package se.diabol.jenkins.pipeline.test;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.Pipeline;
import se.diabol.jenkins.pipeline.domain.Stage;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.status.StatusType;
import se.diabol.jenkins.pipeline.domain.status.promotion.PromotionStatus;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.util.ArrayList;
import java.util.List;

public class PipelineUtil {

    public static Component createComponent(Status status) {
        Task task = new Task(null, "task", "Build", status, null, null, null, true, "");

        List<Task> tasks = new ArrayList<Task>();
        tasks.add(task);
        Stage stage = new Stage("Build", tasks);
        List<Stage> stages = new ArrayList<Stage>();
        stages.add(stage);
        Pipeline pipeline = new Pipeline("Pipeline B", null, null, "1.0.0.1", null, null, null, stages, false);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        pipelines.add(pipeline);
        Component component = new Component("B", "B", "job/A", false, 3, false, 1);
        component.setPipelines(pipelines);
        return component;
    }

    public static Status status(StatusType statusType, DateTime lastRunedAt) {
        return new SimpleStatus(statusType, lastRunedAt.getMillis(), 10, false, Lists.<PromotionStatus>newArrayList());
    }

}
