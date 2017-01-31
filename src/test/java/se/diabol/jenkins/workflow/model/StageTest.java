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

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StageTest {

    @Test
    public void hasNoDownstreamStagesShouldHandleNull() {
        Stage stage = mock(Stage.class);
        when(stage.getDownstreamStages()).thenReturn(null);
        when(stage.hasNoDownstreamStages()).thenCallRealMethod();
        assertThat(stage.hasNoDownstreamStages(), is(true));
    }

    @Test
    public void hasNoDownstreamStagesShouldHandleEmptyList() {
        Stage stage = mock(Stage.class);
        when(stage.getDownstreamStages()).thenReturn(Collections.<Stage>emptyList());
        when(stage.hasNoDownstreamStages()).thenCallRealMethod();
        assertThat(stage.hasNoDownstreamStages(), is(true));
    }

    @Test
    public void hasNoDownstreamStagesShouldHandleDownstreamWhenSet() {
        Stage stage = mock(Stage.class);
        when(stage.getDownstreamStages()).thenReturn(Collections.singletonList(new Stage("stage", null)));
        assertThat(stage.hasNoDownstreamStages(), is(false));
    }

    @Test
    public void shouldGetFirstDownstreamId() {
        Stage stage = mock(Stage.class);
        Stage downstreamStage = mock(Stage.class);
        Long expectedDownstreamId = 123L;
        when(downstreamStage.getId()).thenReturn(expectedDownstreamId);
        when(stage.getDownstreamStages()).thenReturn(Collections.singletonList(downstreamStage));
        when(stage.getFirstDownstreamIdAsList()).thenCallRealMethod();
        assertNotNull(stage.getFirstDownstreamIdAsList());
        assertThat(stage.getFirstDownstreamIdAsList().size(), is(1));
        assertThat(stage.getFirstDownstreamIdAsList().get(0), is(expectedDownstreamId.toString()));
    }

    @Test
    public void getFirstDownstreamIdAsListShouldHandleNull() {
        Stage stage = mock(Stage.class);
        when(stage.getDownstreamStages()).thenReturn(null);
        assertNotNull(stage.getFirstDownstreamIdAsList());
        assertThat(stage.getFirstDownstreamIdAsList().isEmpty(), is(true));
    }

    @Test
    public void shouldGetTaskConnections() {
        Long expectedStageId = 456L;
        Long expectedDownstreamId = 123L;
        Stage stage = mock(Stage.class);
        Stage downstreamStage = mock(Stage.class);
        when(stage.getId()).thenReturn(expectedStageId);
        when(downstreamStage.getId()).thenReturn(expectedDownstreamId);
        when(stage.getDownstreamStages()).thenReturn(Collections.singletonList(downstreamStage));
        when(stage.getFirstDownstreamIdAsList()).thenCallRealMethod();
        when(stage.hasNoDownstreamStages()).thenCallRealMethod();
        when(stage.getTaskConnections()).thenCallRealMethod();

        Map<String, List<String>> taskConnections = stage.getTaskConnections();
        assertNotNull(taskConnections);
        assertThat(taskConnections.size(), is(1));
        assertThat(taskConnections.get(expectedStageId.toString()).size(), is(1));
        assertThat(taskConnections.get(expectedStageId.toString()).get(0), is(expectedDownstreamId.toString()));
    }

    @Test
    public void getTaskConnectionsShouldReturnEmptyMapForNoDownstreams() {
        Stage stage = mock(Stage.class);
        when(stage.hasNoDownstreamStages()).thenReturn(true);
        when(stage.getTaskConnections()).thenCallRealMethod();
        assertNotNull(stage.getTaskConnections());
        assertThat(stage.getTaskConnections().size(), is(0));
    }
}
