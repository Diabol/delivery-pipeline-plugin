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
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
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

import static se.diabol.jenkins.workflow.util.Util.getRunById;

public class Task extends AbstractItem {

    private static final WorkflowApi workflowApi = new WorkflowApi();

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

    public static List<Task> resolve(WorkflowRun build, FlowNode stageStartNode) throws PipelineException {
        List<Task> result = new ArrayList<>();
        List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(stageStartNode);
        List<FlowNode> taskNodes = Util.getTaskNodes(stageNodes);

        if (stageHasTaskNodes(taskNodes)) {
            for (FlowNode taskNode : taskNodes) {
                result.add(resolveTask(build, stageStartNode, taskNode));
            }
        } else {
            Stage stage = getStage(build, stageStartNode);;
            Status stageStatus = resolveStageStatus(build, stage);
            result.add(createStageTask(build, stageStartNode, stageStatus));
        }
        return result;
    }

    private static Task resolveTask(WorkflowRun build, FlowNode stageStartNode, FlowNode taskNode)
            throws PipelineException {
        TaskAction action = taskNode.getAction(TaskAction.class);
        Status status = resolveTaskStatus(build, stageStartNode, taskNode, action);
        return new Task(taskNode.getId(), action.getTaskName(), build.getNumber(), status,
                taskLinkFor(build), null, null,
                StatusType.PAUSED_PENDING_INPUT.equals(status.getType()));
    }

    private static Task createStageTask(WorkflowRun build, FlowNode stageStartNode, Status stageStatus) {
        return new Task(stageStartNode.getId(), stageStartNode.getDisplayName(), build.getNumber(), stageStatus,
                taskLinkFor(build), null, null, StatusType.PAUSED_PENDING_INPUT.equals(stageStatus.getType()));
    }

    private static String taskLinkFor(WorkflowRun build) {
        String taskLink = "job/" + Name.of(build).replace("/", "/job/");
        taskLink += "/" + build.getNumber() + "/";
        return taskLink;
    }

    static boolean stageHasTaskNodes(List<FlowNode> taskNodes) {
        return !taskNodes.isEmpty();
    }

    private static Status resolveStageStatus(WorkflowRun build, Stage stage) throws PipelineException {
        Status stageStatus = WorkflowStatus.of(stage);
        if (stageStatus.isRunning()) {
            stageStatus = runningStatus(build, stage);
        }
        return stageStatus;
    }

    private static Stage getStage(WorkflowRun build, FlowNode stageStartNode) throws PipelineException {
        List<Run> runs = workflowApi.getRunsFor(build.getParent());
        Run run = getRunById(runs, build.getNumber());
        Stage stage = run.getStageByName(stageStartNode.getDisplayName());
        if (stage == null) {
            throw new PipelineException("Could not resolve stage " + stageStartNode.getDisplayName()
                    + " for pipeline " + build.getDisplayName());
        }
        return stage;
    }

    private static Status resolveTaskStatus(WorkflowRun build, FlowNode stageStartNode, FlowNode taskNode, TaskAction taskAction) throws PipelineException {
        Stage stage = getStage(build, stageStartNode);

        Long finishedTime = taskAction.getFinishedTime();
        if (finishedTime != null) {
            long duration = finishedTime - getTaskStartTime(taskNode);
            return new SimpleStatus(StatusType.SUCCESS, finishedTime, duration);
        } else {
            Status stageStatus = resolveStageStatus(build, stage);
            if (stageStatus.isRunning()) {
                return runningStatus(build, stage);
            } else {
                long duration = (stage.startTimeMillis.getMillis() + stage.durationMillis) - getTaskStartTime(taskNode);
                return new SimpleStatus(stageStatus.getType(), stage.startTimeMillis.getMillis() + stage.durationMillis, duration);
            }
        }
    }

    private static long getTaskStartTime(FlowNode taskNode) {
        return taskNode.getAction(TimingAction.class).getStartTime();
    }

    private static Status runningStatus(WorkflowRun build, Stage stage) throws PipelineException {
        int progress = progressOfStage(build, stage);
        return runningStatus(build.getTimeInMillis(), progress);
    }

    private static Status runningStatus(long buildTimestamp, int progress) {
        if (progress > 100) {
            progress = 99;
        }
        return StatusFactory.running(progress, buildTimestamp, System.currentTimeMillis() - buildTimestamp);
    }

    private static int progressOfStage(WorkflowRun build, Stage currentStage) throws PipelineException {
        Run previousRun = workflowApi.lastFinishedRunFor(build.getParent());
        if (previousRun == null || !previousRun.hasStage(currentStage.name)) {
            return 99;
        }

        long stageStartTime = currentStage.startTimeMillis.getMillis();
        long estimatedStageDuration = Stage.getDurationOfStageFromRun(previousRun, currentStage);
        return Progress.calculate(stageStartTime, estimatedStageDuration);
    }
}
