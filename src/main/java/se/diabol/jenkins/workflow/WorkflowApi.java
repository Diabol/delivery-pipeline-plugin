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
package se.diabol.jenkins.workflow;

import com.cloudbees.workflow.rest.external.RunExt;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.workflow.api.Run;

import java.util.ArrayList;
import java.util.List;

public class WorkflowApi {

    public WorkflowApi() {
    }

    public List<Run> getRunsFor(WorkflowJob job) {
        List<Run> runs = new ArrayList<>();
        for (WorkflowRun run : job.getBuilds()) {
            runs.add(new Run(RunExt.create(run)));
        }
        return runs;
    }

    public Run lastFinishedRunFor(WorkflowJob job) throws PipelineException {
        return lastFinishedRun(getRunsFor(job));
    }

    private Run lastFinishedRun(List<Run> runs) {
        for (Run run : runs) {
            if (!"IN_PROGRESS".equals(run.status) && !"PAUSED_PENDING_INPUT".equals(run.status)) {
                return run;
            }
        }
        return null;
    }
}
