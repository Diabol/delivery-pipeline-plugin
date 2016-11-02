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
import hudson.model.Result;
import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.domain.task.ManualStep;
import se.diabol.jenkins.workflow.WorkflowApi;
import se.diabol.jenkins.workflow.api.Run;
import se.diabol.jenkins.workflow.api.Stage;
import se.diabol.jenkins.workflow.step.TaskAction;
import se.diabol.jenkins.workflow.util.Name;
import se.diabol.jenkins.workflow.util.Util;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static se.diabol.jenkins.workflow.util.Util.getRunById;

public class Task extends AbstractItem {

    private final String id;
    private final String link;
    private final Status status;
    private final ManualStep manual;
    private final String buildId;
    private final String description;
    private final static WorkflowApi workflowApi = new WorkflowApi(Jenkins.getInstance());

    public Task(String id, String name, Status status, String link,
                ManualStep manual, String description) {
        super(name);
        this.id = id;
        this.link = link;
        this.status = status;
        this.manual = manual;
        this.buildId = null;
        this.description = description;
    }

    @Exported
    public ManualStep getManualStep() {
        return manual;
    }

    @Exported
    public boolean isManual() {
        return manual != null;
    }

    @Exported
    public String getBuildId() {
        return buildId;
    }

    @Exported
    public String getId() {
        return id;
    }

    @Exported
    public String getLink() {
        return link;
    }

    @Exported
    public Status getStatus() {
        return status;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    public static List<Task> resolve(WorkflowRun build, FlowNode stageStartNode) {
        List<Task> result = new ArrayList<Task>();
        List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(stageStartNode);
        List<FlowNode> taskNodes = Util.getTaskNodes(stageNodes);

        if (!taskNodes.isEmpty()) {
            for (FlowNode flowNode : taskNodes) {
                TaskAction action = flowNode.getAction(TaskAction.class);
                //List<FlowNode> nodesInTask = Util.getTaskSteps(stageNodes, flowNode);
                result.add(new Task(flowNode.getId(), action.getTaskName(), resolveTaskStatus(build, stageStartNode), "", null, null));
            }
        } else {
            Status stageStatus = resolveTaskStatus(build, stageStartNode);
            result.add(new Task(stageStartNode.getId(), stageStartNode.getDisplayName(), stageStatus, "", null, null));
        }
        return result;
    }

    private static Status resolveTaskStatus(WorkflowRun build, FlowNode stageStartNode) {
        List<Run> runs = workflowApi.getRunsFor(Name.of(build));
        Run run = getRunById(runs, build.getNumber());
        se.diabol.jenkins.workflow.api.Stage currentStage = run.getStageByName(stageStartNode.getDisplayName());
        if (currentStage == null) {
            return resolveStatus(build, FlowNodeUtil.getStageNodes(stageStartNode), run.stages);
        } else {
            Status stageStatus = WorkflowStatus.of(currentStage);
            if (stageStatus.isRunning()) {
                stageStatus = runningStatus(build, currentStage);
            }
            return stageStatus;
        }
    }

    private static Status resolveStatus(WorkflowRun build, List<FlowNode> taskNodes, List<Stage> stages) {
        boolean allExecuted = isAllExecuted(taskNodes);
        boolean allIdle = isAllNotExecuted(taskNodes);
        if (Result.FAILURE.equals(build.getResult())) {
            return StatusFactory.failed(getStartTime(taskNodes), getDuration(stages), false, null);
        }
        if (isRunning(taskNodes) && !build.getExecution().isComplete()) {
            return runningStatus(build);
        }
        if (allExecuted) {
            if (failed(Util.head(taskNodes))) {
                return StatusFactory.failed(getStartTime(taskNodes), getDuration(stages), false, null);
            } else {
                return StatusFactory.success(getStartTime(taskNodes), getDuration(stages), false, null);
            }
        } else if (allIdle) {
            return StatusFactory.idle();
        }
        return StatusFactory.idle();
    }

    private static boolean failed(FlowNode node) {
        return node != null && node.getError() != null;
    }

    private static Status runningStatus(WorkflowRun build) {
        long buildTimestamp = build.getTimeInMillis();
        int progress = (int) round(100.0d *
                (currentTimeMillis() - buildTimestamp) / build.getEstimatedDuration());
        return runningStatus(buildTimestamp, progress);
    }

    private static Status runningStatus(WorkflowRun build, Stage stage) {
        int progress = progressOfStage(build, stage);
        return runningStatus(build.getTimeInMillis(), progress);
    }

    private static Status runningStatus(long buildTimestamp, int progress) {
        if (progress > 100) {
            progress = 99;
        }
        return StatusFactory.running(progress, buildTimestamp, currentTimeMillis() - buildTimestamp);
    }

    public static int progressOfStage(WorkflowRun build, Stage currentStage) {
        Run run = workflowApi.lastFinishedRunFor(Name.of(build));
        if (!run.hasStage(currentStage.name)) {
            return 99;
        }
        List<se.diabol.jenkins.workflow.api.Stage> stages = run.getStagesUntil(currentStage.name);
        long projectedDurationUntilCurrentStage = Util.sumDurationsOf(stages);

        return (int) round(100.0d
                * (currentTimeMillis() - build.getTimeInMillis())
                / (projectedDurationUntilCurrentStage));
    }

    private static boolean isAllExecuted(List<FlowNode> nodes) {
        for (FlowNode node : nodes) {
            if (!NotExecutedNodeAction.isExecuted(node)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRunning(List<FlowNode> nodes) {
        for (FlowNode node : nodes) {
            if (node.isRunning()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllNotExecuted(List<FlowNode> nodes) {
        for (FlowNode node : nodes) {
            if (NotExecutedNodeAction.isExecuted(node)) {
                return false;
            }
        }
        return true;
    }

    private static long getStartTime(List<FlowNode> nodes) {
        if (!nodes.isEmpty()) {
            return TimingAction.getStartTime(nodes.get(0));
        }
        return 0;
    }

    private static long getDuration(List<Stage> stages) {
        long result = 0;
        for (Stage stage : stages) {
            result = result + stage.durationMillis;
        }
        return result;
    }
}
