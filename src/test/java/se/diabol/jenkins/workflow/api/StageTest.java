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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StageTest {

    private static final Long expectedStageDuration = 1500L;

    @Test
    public void identicalStagesShouldBeConsideredEqual() {
        Stage stage1 = getPopulatedStage();
        Stage stage2 = new Stage(stage1.id, stage1.name, stage1.status, stage1.startTimeMillis, stage1.durationMillis);

        assertTrue(stage1.equals(stage2));
        assertTrue(stage2.equals(stage1));
        assertEquals(stage1.toString(), stage2.toString());
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldIdentifyNonEqualStages() {
        Stage stage1 = getPopulatedStage();
        Stage stage2 = new Stage("uniqueId", "uniqueName", null, null, 5000L);

        assertFalse(stage1.equals(stage2));
        assertFalse(stage2.equals(stage1));
        assertFalse(stage1.equals(null));
        assertFalse(stage2.equals(null));
    }
    
    @Test
    public void shouldGetDurationOfStageFromPreviousRun() {
        long expectedDuration = 1000L;
        Stage stageWithDuration = new Stage("stageId", "stageName", "SUCCESS", null, expectedDuration);
        Run previousRun = mock(Run.class);
        when(previousRun.getStageByName(anyString())).thenReturn(stageWithDuration);

        assertThat(Stage.getDurationOfStageFromRun(previousRun, getPopulatedStage()), is(expectedDuration));
    }
    
    @Test
    public void getDurationOfStageFromRunShouldReturnDefaultWhenPreviousRunDidNotContainStage() {
        Stage stageWithUnknownDuration = new Stage("stageId", "stageName", "SUCCESS", null, null);
        Run previousRun = mock(Run.class);
        when(previousRun.getStageByName(anyString())).thenReturn(stageWithUnknownDuration);

        assertThat(Stage.getDurationOfStageFromRun(previousRun, getPopulatedStage()), is(-1L));
    }

    @Test
    public void getDurationOfStageFromRunShouldReturnDefaultWhenDurationNotKnown() {
        Run previousRun = mock(Run.class);
        when(previousRun.getStageByName(anyString())).thenReturn(null);

        assertThat(Stage.getDurationOfStageFromRun(previousRun, getPopulatedStage()), is(-1L));
    }

    @Test
    public void shouldGetDurationForEmptyParameter() {
        long duration = Stage.getDurationOf(Collections.<Stage>emptyList());
        assertThat(duration, is(0L));
    }

    @Test
    public void shouldGetDuration() {
        List<Stage> stages = stages();
        long duration = Stage.getDurationOf(stages);
        assertThat(duration, is(expectedStageDuration * stages.size()));
    }

    @Test
    public void shouldGetDurationForNullParameter() {
        long duration = Stage.getDurationOf(null);
        assertThat(duration, is(0L));
    }

    private Stage getPopulatedStage() {
        String id = "stageId";
        String name = "stageName";
        String status = "SUCCESS";
        DateTime startTimeMillis = new DateTime(System.currentTimeMillis());
        Long durationMillis = expectedStageDuration;
        return new Stage(id, name, status, startTimeMillis, durationMillis);
    }

    private static List<Stage> stages() {
        List<Stage> stages = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            stages.add(stage("stage" + i));
        }
        return stages;
    }

    private static Stage stage(String name) {
        return new Stage(name, name, "SUCCESS", new DateTime(0L), expectedStageDuration);
    }

}
