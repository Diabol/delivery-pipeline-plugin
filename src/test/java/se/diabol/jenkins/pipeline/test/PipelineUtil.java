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
