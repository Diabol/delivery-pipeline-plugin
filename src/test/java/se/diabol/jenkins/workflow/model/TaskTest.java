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
package se.diabol.jenkins.workflow.model;

import com.google.api.client.util.DateTime;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.diabol.jenkins.workflow.api.Stage;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TaskTest {

    private final static Long expectedStageDuration = 1000L;

    @Test
    public void isRunningShouldHandleNullParameter() {
        assertThat(Task.isRunning(null), is(false));
    }

    @Test
    public void isRunningShouldHandleEmptyParameter() {
        assertThat(Task.isRunning(Collections.<FlowNode>emptyList()), is(false));
    }

    @Test
    public void shouldGetStartTime() {
        long expectedStartTime = 123L;
        List<FlowNode> flowNodes = new ArrayList<FlowNode>(1);
        FlowNode flowNode = mock(FlowNode.class);
        TimingAction timingAction = mock(TimingAction.class);
        when(timingAction.getStartTime()).thenReturn(expectedStartTime);
        when(flowNode.getAction(TimingAction.class)).thenReturn(timingAction);
        flowNodes.add(flowNode);

        assertThat(Task.getStartTime(flowNodes), is(expectedStartTime));
    }

    @Test
    public void shouldGetStartTimeForNullParameter() {
        assertThat(Task.getStartTime(null), is(0L));
    }

    @Test
    public void shouldGetStartTimeForEmptyParameter() {
        assertThat(Task.getStartTime(Collections.<FlowNode>emptyList()), is(0L));
    }

    @Test
    public void shouldGetDuration() {
        List<Stage> stages = stages();
        long duration = Task.getDuration(stages);
        assertThat(duration, is(expectedStageDuration * stages.size()));
    }

    @Test
    public void shouldGetDurationForNullParameter() {
        long duration = Task.getDuration(null);
        assertThat(duration, is(0L));
    }

    @Test
    public void shouldGetDurationForEmptyParameter() {
        long duration = Task.getDuration(Collections.<Stage>emptyList());
        assertThat(duration, is(0L));
    }

    private static List<Stage> stages() {
        List<Stage> stages = new ArrayList<Stage>(5);
        for (int i = 0; i < 5; i++) {
            stages.add(stage("stage" + i));
        }
        return stages;
    }

    private static Stage stage(String name) {
        return new Stage(null,
                name,
                name,
                "SUCCESS",
                new DateTime(0L),
                expectedStageDuration);
    }
}
