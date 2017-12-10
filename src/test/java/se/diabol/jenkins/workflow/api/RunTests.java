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

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RunTests {

    private static final List<Stage> stages = stageFixture();
    private static final Run run = runFixture();

    @Test
    public void shouldHaveStage() {
        assertTrue(run.hasStage("Stage1"));
        assertTrue(run.hasStage("Stage2"));
        assertTrue(run.hasStage("Stage3"));
        assertTrue(run.hasStage("Stage4"));
        assertTrue(run.hasStage("Stage5"));
    }

    @Test
    public void shouldNotHaveStage() {
        assertFalse(run.hasStage("Stage0"));
        assertFalse(run.hasStage("Stage6"));
        assertFalse(run.hasStage("ArbitraryNonExistingName"));
    }

    @Test
    public void shouldFindStageByName() {
        assertThat(run.getStageByName("Stage1"), is(stages.get(0)));
        assertThat(run.getStageByName("Stage2"), is(stages.get(1)));
        assertThat(run.getStageByName("Stage3"), is(stages.get(2)));
        assertThat(run.getStageByName("Stage4"), is(stages.get(3)));
        assertThat(run.getStageByName("Stage5"), is(stages.get(4)));
    }

    @Test
    public void shouldNotFindNonExistingStageByName() {
        assertThat(run.getStageByName("Stage0"), is(nullValue()));
        assertThat(run.getStageByName("Stage6"), is(nullValue()));
        assertThat(run.getStageByName("ArbitraryNonExistingName"), is(nullValue()));
    }

    private static Run runFixture() {
        return new Run(
                "2014-04-27_20-40-00",
                       "#1",
                       "SUCCESS",
                       new DateTime(System.currentTimeMillis()),
                       new DateTime(System.currentTimeMillis()),
                       7500L,
                       stages);
    }

    private static List<Stage> stageFixture() {
        List<Stage> stages = new ArrayList<>(5);
        for (int i = 1; i <= 5; i = i + 1) {
            stages.add(new Stage("2014-04-27_20-40-00",
                                 "Stage" + i,
                                 "SUCCESS",
                                 new DateTime(System.currentTimeMillis()),
                                 1500L));
        }
        return stages;
    }


}
