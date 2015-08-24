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
package se.diabol.jenkins.pipeline.sort;

import org.junit.Test;

import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.Pipeline;
import se.diabol.jenkins.pipeline.domain.Stage;
import se.diabol.jenkins.pipeline.domain.status.promotion.PromotionStatus;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LatestActivityComparatorTest {

    @Test
    public void testCompare() {
        Task taskA = new Task(null, "task", "Build", StatusFactory.success(0, 20, false,
                Collections.<PromotionStatus>emptyList()), null, null, null, true, "");

        List<Task> tasksA = new ArrayList<Task>();
        tasksA.add(taskA);
        Stage stageA = new Stage("Build", tasksA);
        List<Stage> stagesA = new ArrayList<Stage>();
        stagesA.add(stageA);
        Pipeline pipelineA = new Pipeline("Pipeline A", null, null, "1.0.0.1", null, null, null, stagesA, false);
        List<Pipeline> pipelinesA = new ArrayList<Pipeline>();
        pipelinesA.add(pipelineA);

        Task taskB = new Task(null, "task", "Build", StatusFactory.success(10, 20, false,
                Collections.<PromotionStatus>emptyList()), null, null, null, true, "");

        List<Task> tasksB = new ArrayList<Task>();
        tasksB.add(taskB);
        Stage stageB = new Stage("Build", tasksB);
        List<Stage> stagesB = new ArrayList<Stage>();
        stagesB.add(stageB);
        Pipeline pipelineB = new Pipeline("Pipeline B", null, null, "1.0.0.1", null, null, null, stagesB, false);
        List<Pipeline> pipelinesB = new ArrayList<Pipeline>();
        pipelinesB.add(pipelineB);


        Component componentB = new Component("B", "B", "job/A", false, pipelinesB);
        Component componentA = new Component("A", "A", "job/B", false, pipelinesA);
        List<Component> list = new ArrayList<Component>();
        list.add(componentB);
        list.add(componentA);
        Collections.sort(list, new LatestActivityComparator.DescriptorImpl().createInstance());
        assertEquals(componentB, list.get(0));
        assertEquals(componentA, list.get(1));
    }

}
