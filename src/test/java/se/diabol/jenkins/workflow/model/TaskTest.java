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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Test;

import java.util.Collections;

public class TaskTest {

    @Test
    public void taskNodesDefinedInStageShouldReturnTrueWhenListPopulated() {
        assertThat(Task.taskNodesDefinedInStage(Collections.singletonList(mock(FlowNode.class))), is(true));
    }

    @Test
    public void taskNodesDefinedInStageShouldReturnFalseForEmptyList() {
        assertThat(Task.taskNodesDefinedInStage(Collections.<FlowNode>emptyList()), is(false));
    }
}
