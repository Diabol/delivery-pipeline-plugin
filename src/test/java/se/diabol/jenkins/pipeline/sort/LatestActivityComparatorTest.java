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
import se.diabol.jenkins.pipeline.model.Component;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;
import se.diabol.jenkins.pipeline.model.status.StatusFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LatestActivityComparatorTest {

    @Test
    public void testCompare() {
        Task taskA = new Task("task", "Build", "#1", StatusFactory.success(0, 20), "", false, null);
        List<Task> tasksA = new ArrayList<Task>();
        tasksA.add(taskA);
        Stage stageA = new Stage("Build", tasksA);
        List<Stage> stagesA = new ArrayList<Stage>();
        stagesA.add(stageA);
        Pipeline pipelineA = new Pipeline("Pipeline A", "1.0.0.1", null, null, Collections.EMPTY_LIST, null, stagesA, false);
        List<Pipeline> pipelinesA = new ArrayList<Pipeline>();
        pipelinesA.add(pipelineA);

        Task taskB = new Task("task", "Build", "#1", StatusFactory.success(10, 20), "", false, null);
        List<Task> tasksB = new ArrayList<Task>();
        tasksB.add(taskB);
        Stage stageB = new Stage("Build", tasksB);
        List<Stage> stagesB = new ArrayList<Stage>();
        stagesB.add(stageB);
        Pipeline pipelineB = new Pipeline("Pipeline B", "1.0.0.1", null, null, Collections.EMPTY_LIST, null, stagesB, false);
        List<Pipeline> pipelinesB = new ArrayList<Pipeline>();
        pipelinesB.add(pipelineB);


        Component componentB = new Component("B", pipelinesB);
        Component componentA = new Component("A", pipelinesA);
        List<Component> list = new ArrayList<Component>();
        list.add(componentB);
        list.add(componentA);
        Collections.sort(list, new LatestActivityComparator.DescriptorImpl().createInstance());
        assertEquals(componentB, list.get(0));
        assertEquals(componentA, list.get(1));
    }

}
