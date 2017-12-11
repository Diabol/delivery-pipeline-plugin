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

import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
import se.diabol.jenkins.pipeline.domain.status.StatusType;
import se.diabol.jenkins.workflow.api.Stage;

public class WorkflowStatus extends SimpleStatus {

    public WorkflowStatus(StatusType type, long lastActivity, long duration) {
        super(type, lastActivity, duration);
    }

    public static WorkflowStatus of(Stage stage) {
        return new WorkflowStatus(
                statusType(stage), stage.startTimeMillis.getMillis() + stage.durationMillis, stage.durationMillis);
    }

    static StatusType statusType(Stage currentStage) {
        StatusType statusType;
        if (currentStage == null || currentStage.status == null) {
            return StatusType.NOT_BUILT;
        } else if (currentStage.status.equals("IN_PROGRESS")) {
            statusType = StatusType.RUNNING;
        } else if (currentStage.status.equals("ABORTED")) {
            statusType = StatusType.CANCELLED;
        } else {
            statusType = StatusType.valueOf(currentStage.status);
        }
        return statusType;
    }
}
