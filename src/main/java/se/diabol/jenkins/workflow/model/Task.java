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
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.results.StaticAnalysisResult;
import se.diabol.jenkins.pipeline.domain.results.TestResult;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.domain.task.ManualStep;
import se.diabol.jenkins.workflow.step.TaskAction;
import se.diabol.jenkins.workflow.util.Util;

public class Task extends AbstractItem {

    private final String id;
    private final String link;
    private final List<TestResult> testResults;
    private final List<StaticAnalysisResult> staticAnalysisResults;
    private final Status status;
    private final ManualStep manual;
    private final String buildId;
    private final String description;

    public Task(String id, String name, Status status, String link,
                ManualStep manual, String description) {
        super(name);
        this.id = id;
        this.link = link;
        this.testResults = null;
        this.staticAnalysisResults = null;
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
    public List<TestResult> getTestResults() {
        return testResults;
    }

    @Exported
    public List<StaticAnalysisResult> getStaticAnalysisResults() {
        return staticAnalysisResults;
    }

    @Exported
    public Status getStatus() {
        return status;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    @Exported
    public boolean isRebuildable() {
        return false;
    }


    public static List<Task> resolve(WorkflowRun build, FlowNode stageStartNode) {
        List<Task> result = new ArrayList<Task>();

        List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(stageStartNode);

        List<FlowNode> taskNodes = Util.getTaskNodes(stageNodes);

        for (FlowNode flowNode : taskNodes) {
            TaskAction action = flowNode.getAction(TaskAction.class);
            List<FlowNode> tasks = Util.getTaskNodes(stageNodes, flowNode);

            result.add(new Task(flowNode.getId(), action.getTaskName(), resolveStatus(build, tasks), "", null, null));
        }
        return result;
    }


    private static Status resolveStatus(WorkflowRun build, List<FlowNode> taskNodes) {
        boolean allExecuted = isAllExecuted(taskNodes);
        boolean allIdle = isAllNotExecuted(taskNodes);
        if (Result.FAILURE.equals(build.getResult())) {
            return StatusFactory.failed(0, 0,false, null);
        }
        if (isRunning(taskNodes) && !build.getExecution().isComplete()) {
            return StatusFactory.running(99, 0, 0);
        }




        if (allExecuted) {
            for (int i = 0; i < taskNodes.size(); i++) {
                FlowNode node = taskNodes.get(i);
                ErrorAction errorAction = node.getError();
                if (errorAction != null) {
                    return StatusFactory.failed(0, 0,false, null);
                }
                long duration = getDuration(taskNodes);
                return StatusFactory.success(getStartTime(taskNodes), duration, false, null);
            }
        } else {

            if (allIdle) {
                return StatusFactory.idle();
            }
        }
        return StatusFactory.idle();

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

    private static long getDuration(List<FlowNode> nodes) {
        long result = 0;
        for (FlowNode node : nodes) {
            result = result + FlowNodeUtil.getNodeExecDuration(node);
        }
        return result;
    }
}
