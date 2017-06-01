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

import static java.lang.Math.round;
import static se.diabol.jenkins.workflow.util.Util.getRunById;

import com.cloudbees.workflow.flownode.FlowNodeUtil;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.domain.status.StatusType;
import se.diabol.jenkins.pipeline.domain.task.ManualStep;
import se.diabol.jenkins.workflow.WorkflowApi;
import se.diabol.jenkins.workflow.api.Run;
import se.diabol.jenkins.workflow.api.Stage;
import se.diabol.jenkins.workflow.step.TaskAction;
import se.diabol.jenkins.workflow.util.Name;
import se.diabol.jenkins.workflow.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Task extends AbstractItem {

    private static final WorkflowApi workflowApi = new WorkflowApi(Jenkins.getInstance());

    private final String id;
    private final int buildId;
    private final String link;
    private final Status status;
    private final ManualStep manual;
    private final String description;
    private final boolean requiringInput;

    public Task(String id,
                String name,
                int buildId,
                Status status,
                String link,
                ManualStep manual,
                String description,
                boolean requiringInput) {
        super(name);
        this.id = id;
        this.buildId = buildId;
        this.status = status;
        this.link = link;
        this.manual = manual;
        this.description = description;
        this.requiringInput = requiringInput;
    }

    @Exported
    public String getId() {
        return id;
    }

    @Exported
    public Integer getBuildId() {
        return buildId;
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
    public ManualStep getManualStep() {
        return manual;
    }

    @Exported
    public boolean isManual() {
        return manual != null;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    @Exported
    public boolean isRequiringInput() {
        return requiringInput;
    }

    public static List<Task> resolve(WorkflowRun build, FlowNode stageStartNode) {
        List<Task> result = new ArrayList<Task>();
        List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(stageStartNode);
        List<FlowNode> taskNodes = Util.getTaskNodes(stageNodes);

        if (taskNodesDefinedInStage(taskNodes)) {
            for (FlowNode flowNode : taskNodes) {
                TaskAction action = flowNode.getAction(TaskAction.class);
                Status status = resolveTaskStatus(build, stageStartNode);
                result.add(new Task(flowNode.getId(), action.getTaskName(), build.getNumber(), status,
                                    taskLinkFor(build), null, null,
                                    StatusType.PAUSED_PENDING_INPUT.equals(status.getType())));
            }
        } else {
            Status stageStatus = resolveTaskStatus(build, stageStartNode);
            result.add(createStageTask(build, stageStartNode, stageStatus));
        }
        return result;
    }

    private static Task createStageTask(WorkflowRun build, FlowNode stageStartNode, Status stageStatus) {
        return new Task(stageStartNode.getId(), stageStartNode.getDisplayName(), build.getNumber(), stageStatus,
                taskLinkFor(build), null, null, StatusType.PAUSED_PENDING_INPUT.equals(stageStatus.getType()));
    }

    private static String taskLinkFor(WorkflowRun build) {
        return "job/" + Name.of(build);
    }

    static boolean taskNodesDefinedInStage(List<FlowNode> taskNodes) {
        return !taskNodes.isEmpty();
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
        if (Result.FAILURE.equals(build.getResult())) {
            return StatusFactory.failed(getStartTime(taskNodes), getDuration(stages), false, null);
        }
        if (isRunning(taskNodes) && !build.getExecution().isComplete()) {
            return runningStatus(build);
        }
        if (allExecuted(taskNodes)) {
            if (failed(Util.head(taskNodes))) {
                return StatusFactory.failed(getStartTime(taskNodes), getDuration(stages), false, null);
            } else {
                return StatusFactory.success(getStartTime(taskNodes), getDuration(stages), false, null);
            }
        } else {
            return StatusFactory.idle();
        }
    }

    protected static boolean failed(FlowNode node) {
        return node != null && node.getError() != null;
    }

    private static Status runningStatus(WorkflowRun build) {
        long buildTimestamp = build.getTimeInMillis();
        int progress = calculateProgress(buildTimestamp, build.getEstimatedDuration());
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
        return StatusFactory.running(progress, buildTimestamp, System.currentTimeMillis() - buildTimestamp);
    }

    private static int progressOfStage(WorkflowRun build, Stage currentStage) {
        Run previousRun = workflowApi.lastFinishedRunFor(Name.of(build));
        if (previousRun == null || !previousRun.hasStage(currentStage.name)) {
            return 99;
        }

        long stageStartTime = currentStage.startTimeMillis.getValue();
        long estimatedStageDuration = Stage.getDurationOfStageFromRun(previousRun, currentStage);
        return calculateProgress(stageStartTime, estimatedStageDuration);
    }

    static int calculateProgress(long timestampFromBuild, long estimatedDuration) {
        return (int) round(100.0d
                * (System.currentTimeMillis() - timestampFromBuild)
                / estimatedDuration);
    }

    private static boolean allExecuted(List<FlowNode> nodes) {
        for (FlowNode node : nodes) {
            if (!NotExecutedNodeAction.isExecuted(node)) {
                return false;
            }
        }
        return true;
    }

    static boolean isRunning(List<FlowNode> nodes) {
        if (nodes != null) {
            for (FlowNode node : nodes) {
                if (node.isRunning()) {
                    return true;
                }
            }
        }
        return false;
    }

    static long getStartTime(List<FlowNode> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            return TimingAction.getStartTime(nodes.get(0));
        }
        return 0;
    }

    protected static long getDuration(List<Stage> stages) {
        long result = 0;
        if (stages != null) {
            for (Stage stage : stages) {
                result = result + stage.durationMillis;
            }
        }
        return result;
    }
}
