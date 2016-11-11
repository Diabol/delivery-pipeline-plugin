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
import org.junit.Test;
import se.diabol.jenkins.pipeline.domain.status.StatusType;
import se.diabol.jenkins.workflow.api.Stage;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WorkflowStatusTest {

    @Test
    public void ofShouldReturnWorkflowStatus() throws Exception {
        Stage stage = getStageWithStatus("RUNNING");
        WorkflowStatus status = WorkflowStatus.of(stage);
        assertThat(status.getDuration(), is(stage.durationMillis));
        assertThat(status.getType().toString(), is(stage.status));
    }

    @Test
    public void nullStageShouldYieldNotBuiltStatusType() throws Exception {
        assertThat(WorkflowStatus.statusType(null), is(StatusType.NOT_BUILT));
    }

    @Test
    public void stageWithNoStatusShouldYieldNotBuiltStatusType() throws Exception {
        assertThat(WorkflowStatus.statusType(null), is(StatusType.NOT_BUILT));
    }

    @Test
    public void stageWithInProgressStatusShouldYieldRunningStatusType() throws Exception {
        Stage stageInProgress = getStageWithStatus("IN_PROGRESS");
        assertThat(WorkflowStatus.statusType(stageInProgress), is(StatusType.RUNNING));
    }

    @Test
    public void stageWithAbortedStatusShouldYieldCancelledStatusType() throws Exception {
        Stage stageInProgress = getStageWithStatus("ABORTED");
        assertThat(WorkflowStatus.statusType(stageInProgress), is(StatusType.CANCELLED));
    }

    @Test
    public void stageWithFailedStatusShouldYieldFailedStatusType() throws Exception {
        Stage failedStage = getStageWithStatus("FAILED");
        assertThat(WorkflowStatus.statusType(failedStage), is(StatusType.FAILED));
    }

    private static Stage getStageWithStatus(String status) {
        return new Stage(null, "id", "name", status, new DateTime(System.currentTimeMillis()), 100L);
    }

}