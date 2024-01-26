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

import static java.util.Collections.singletonList;
import static se.diabol.jenkins.workflow.util.Util.head;

import com.google.common.collect.ImmutableList;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.core.AbstractItem;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage extends AbstractItem {

    private List<Task> tasks;

    private String version;
    private int row;
    private int column;
    private List<Stage> downstreamStages;
    private List<Long> downstreamStageIds;
    private long id;

    public Stage(String name, List<Task> tasks) {
        super(name);
        this.tasks = immutableListOf(tasks);
        this.id = PipelineUtils.getRandom();
    }

    private List<Task> immutableListOf(List<Task> tasks) {
        return tasks == null ? Collections.<Task>emptyList() : ImmutableList.copyOf(tasks);
    }

    @Exported
    public List<Task> getTasks() {
        return tasks;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
    public List<Stage> getDownstreamStages() {
        return downstreamStages;
    }

    @Exported
    public Map<String, List<String>> getTaskConnections() {
        if (hasNoDownstreamStages()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> taskIdToConnectionsMap = new HashMap<>();
        taskIdToConnectionsMap.put("" + getId(), getFirstDownstreamIdAsList());
        return taskIdToConnectionsMap;
    }

    protected List<String> getFirstDownstreamIdAsList() {
        Stage first = head(getDownstreamStages());
        if (first == null) {
            return Collections.emptyList();
        }
        return singletonList(first.getId() + "");
    }

    protected boolean hasNoDownstreamStages() {
        return getDownstreamStages() == null || getDownstreamStages().isEmpty();
    }

    @Exported
    public long getId() {
        return id;
    }

    @Exported
    public List<Long> getDownstreamStageIds() {
        return downstreamStageIds;
    }

    static List<Stage> extractStages(WorkflowRun build, List<FlowNode> stageNodes) throws PipelineException {
        List<Stage> result = resolveStageNodes(build, stageNodes);
        for (int i = 0; i < result.size(); i++) {
            Stage stage = result.get(i);
            if (i + 1 < result.size()) {
                stage.downstreamStages = singletonList(result.get(i + 1));
                stage.downstreamStageIds = singletonList(result.get(i + 1).getId());
            }
            stage.setColumn(i);
            stage.setRow(0);
        }
        return result;
    }

    private static List<Stage> resolveStageNodes(WorkflowRun build, List<FlowNode> stageNodes)
            throws PipelineException {
        List<Stage> result = new ArrayList<>();
        for (FlowNode stageNode : stageNodes) {
            List<Task> tasks = Task.resolve(build, stageNode);
            result.add(new Stage(stageNode.getDisplayName(), tasks));
        }
        return result;
    }
}
