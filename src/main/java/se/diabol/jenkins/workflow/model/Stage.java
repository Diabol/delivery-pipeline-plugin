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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

import static java.util.Collections.singletonList;

public class Stage extends AbstractItem {

    private List<Task> tasks;

    private String version;
    private int row;
    private int column;
    private Map<String, List<String>> taskConnections;
    private List<String> downstreamStages;
    private List<Long> downstreamStageIds;
    private long id;

    public Stage(String name, List<Task> tasks) {
        super(name);
        this.tasks = ImmutableList.copyOf(tasks);
        this.id = PipelineUtils.getRandom();
    }

    private Stage(Stage stage, List<Task> tasks, String version, long id) {
        this(stage.getName(), tasks, stage.getDownstreamStages(), stage.getDownstreamStageIds(), stage.getTaskConnections(), version,
                stage.getRow(), stage.getColumn(), id);
    }

    private Stage(String name, List<Task> tasks, List<String> downstreamStages, List<Long> downstreamStageIds, Map<String,
            List<String>> taskConnections, String version, int row, int column, long id) {
        super(name);
        this.tasks = tasks;
        this.version = version;
        this.row = row;
        this.column = column;
        this.downstreamStages = downstreamStages;
        this.taskConnections = taskConnections;
        this.downstreamStageIds = downstreamStageIds;
        this.id = id;
    }

    @Exported
    public List<Task> getTasks() {
        return tasks;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    @Exported
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Exported
    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    @Exported
    public List<String> getDownstreamStages() {
        return downstreamStages;
    }

    public void setDownstreamStages(List<String> downstreamStages) {
        this.downstreamStages = downstreamStages;
    }

    @Exported
    public Map<String, List<String>> getTaskConnections() {
        return taskConnections;
    }

    @Exported
    public long getId() {
        return id;
    }

    @Exported
    public List<Long> getDownstreamStageIds() {
        return downstreamStageIds;
    }

    public void setDownstreamStageIds(List<Long> downstreamStageIds) {
        this.downstreamStageIds = downstreamStageIds;
    }

    public void setTaskConnections(Map<String, List<String>> taskConnections) {
        this.taskConnections = taskConnections;
    }


    public static List<Stage> extractStages(WorkflowRun build, List<FlowNode> stageNodes) throws PipelineException {
        List<Stage> result = new ArrayList<Stage>();
        for (FlowNode stageNode : stageNodes) {
            List<Task> tasks = Task.resolve(build, stageNode);
            result.add(new Stage(stageNode.getDisplayName(), tasks));
        }
        for (int i = 0; i < result.size(); i++) {
            Stage stage = result.get(i);
            if (i + 1 < result.size()) {
                stage.downstreamStageIds = singletonList(result.get(i + 1).getId());
            }
            stage.setColumn(i);
            stage.setRow(0);

        }

        return result;
    }


}
