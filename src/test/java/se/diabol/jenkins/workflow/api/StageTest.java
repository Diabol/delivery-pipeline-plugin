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

import com.google.api.client.util.DateTime;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StageTest {

    @Test
    public void identicalStagesShouldBeConsideredEqual() {
        Stage stage1 = getPopulatedStage();
        Stage stage2 = new Stage(stage1._links, stage1.id, stage1.name, stage1.status, stage1.startTimeMillis, stage1.durationMillis);

        assertTrue(stage1.equals(stage2));
        assertTrue(stage2.equals(stage1));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldIdentifyNonEqualStages() {
        Stage stage1 = getPopulatedStage();
        Stage stage2 = new Stage(null, "uniqueId", "uniqueName", null, null, 5000L);

        assertFalse(stage1.equals(stage2));
        assertFalse(stage2.equals(stage1));
        assertFalse(stage1.equals(null));
        assertFalse(stage2.equals(null));
    }

    private Stage getPopulatedStage() {
        Map<String, ?> links = Collections.emptyMap();
        String id = "stageId";
        String name = "stageName";
        String status = "SUCCESS";
        DateTime startTimeMillis = new DateTime(System.currentTimeMillis());
        Long durationMillis = 1500L;
        return new Stage(links, id, name, status, startTimeMillis, durationMillis);
    }


}
