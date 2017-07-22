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
package se.diabol.jenkins.workflow.api;

import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodesTest {

    @Test
    public void areRunningShouldHandleNullParameter() {
        assertThat(Nodes.areRunning(null), is(false));
    }

    @Test
    public void areRunningShouldHandleEmptyParameter() {
        assertThat(Nodes.areRunning(Collections.<FlowNode>emptyList()), is(false));
    }

    @Test
    public void areRunningShouldRecognizeRunningNode() {
        FlowNode node = mock(FlowNode.class);
        when(node.getExecution()).thenReturn(mock(FlowExecution.class));
        when(node.isRunning()).thenReturn(true);
        assertThat(Nodes.areRunning(Collections.singletonList(node)), is(true));
    }

    @Test
    public void areRunningShouldRecognizeNonRunningNode() {
        FlowNode node = mock(FlowNode.class);
        when(node.getExecution()).thenReturn(mock(FlowExecution.class));
        when(node.isRunning()).thenReturn(false);
        assertThat(Nodes.areRunning(Collections.singletonList(node)), is(false));
    }

    @Test
    public void failedShouldRecognizeFailedFlowNode() {
        FlowNode failedNode = mock(FlowNode.class);
        when(failedNode.getError()).thenReturn(new ErrorAction(new IllegalStateException("Test created exception")));
        assertThat(Nodes.firstFailed(Collections.singletonList(failedNode)), is(true));
    }

    @Test
    public void failedShouldRecognizeNonFailedFlowNode() {
        FlowNode successfulNode = mock(FlowNode.class);
        when(successfulNode.getError()).thenReturn(null);
        assertThat(Nodes.firstFailed(Collections.singletonList(successfulNode)), is(false));
    }

    @Test
    public void areAllExecutedShouldRecognizeExecutedFlowNode() {
        FlowNode node = mock(FlowNode.class);
        when(node.getAction(NotExecutedNodeAction.class)).thenReturn(null);
        assertThat(Nodes.areAllExecuted(Collections.singletonList(node)), is(true));
    }

    @Test
    public void areAllExecutedShouldRecognizeNonExecutedFlowNode() {
        FlowNode node = mock(FlowNode.class);
        when(node.getAction(NotExecutedNodeAction.class)).thenReturn(mock(NotExecutedNodeAction.class));
        assertThat(Nodes.areAllExecuted(Collections.singletonList(node)), is(false));
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

        assertThat(Nodes.getStartTime(flowNodes), is(expectedStartTime));
    }

    @Test
    public void shouldGetStartTimeForNullParameter() {
        assertThat(Nodes.getStartTime(null), is(0L));
    }

    @Test
    public void shouldGetStartTimeForEmptyParameter() {
        assertThat(Nodes.getStartTime(Collections.<FlowNode>emptyList()), is(0L));
    }
}
