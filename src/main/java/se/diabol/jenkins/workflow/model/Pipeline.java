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

import com.cloudbees.workflow.flownode.FlowNodeUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.Change;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.pipeline.domain.TriggerCause;
import se.diabol.jenkins.pipeline.domain.UserInfo;
import se.diabol.jenkins.pipeline.domain.task.Task;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

public class Pipeline extends AbstractItem {

    private List<Stage> stages;

    private String version;

    private List<TriggerCause> triggeredBy;
    private Set<UserInfo> contributors;

    private String timestamp;

    private List<Change> changes;

    private long totalBuildTime;

    private Map<String, Task> allTasks = null;

    public Pipeline(String name, String version, List<Stage> stages, List<Change> changes,
                    List<TriggerCause> triggeredBy, String timestamp) {
        super(name);
        this.stages = stages;
        this.version = version;
        this.changes = changes;
        this.triggeredBy = triggeredBy;
        this.timestamp = timestamp;
    }

    @Exported
    public List<Stage> getStages() {
        return stages;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    @Exported
    public String getTimestamp() {
        return timestamp;
    }

    @Exported
    public boolean isAggregated() {
        return false;
    }

    @Exported
    public Set<UserInfo> getContributors() {
        return contributors;
    }

    @Exported
    public int getId() {
        return hashCode();
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    @Exported
    public List<Change> getChanges() {
        return changes;
    }

    @Exported
    public long getTotalBuildTime() {
        return totalBuildTime;
    }

    @Exported
    public List<TriggerCause> getTriggeredBy() {
        return triggeredBy;
    }


    public static Pipeline resolve(WorkflowJob project, WorkflowRun build) throws PipelineException {
        String pipeLineTimestamp = PipelineUtils.formatTimestamp(build.getTimeInMillis());

        List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(build.getExecution());
        return new Pipeline("Pipeline", build.getDisplayName(), Stage.extractStages(build, stageNodes),
                Change.getChanges(build.getChangeSets()), TriggerCause.getTriggeredBy(project, build),
                pipeLineTimestamp);
    }

}
