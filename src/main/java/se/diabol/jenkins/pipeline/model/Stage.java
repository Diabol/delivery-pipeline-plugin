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
package se.diabol.jenkins.pipeline.model;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.toStringHelper;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Stage extends AbstractItem {
    private List<Task> tasks;

    private String version;
    private int row;
    private int column;
    private Map<String, List<String>> taskConnections;
    private List<String> downstreamStages;

    public Stage(String name, List<Task> tasks, List<String> downstreamStages, Map<String, List<String>> taskConnections) {
        super(name);
        this.tasks = ImmutableList.copyOf(tasks);
        this.downstreamStages = downstreamStages;
        this.taskConnections = taskConnections;
    }

    public Stage(String name, List<Task> tasks, List<String> downstreamStages, Map<String, List<String>> taskConnections, String version, int row, int column) {
        super(name);
        this.tasks = tasks;
        this.version = version;
        this.row = row;
        this.column = column;
        this.downstreamStages = downstreamStages;
        this.taskConnections = taskConnections;
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

    public void setTaskConnections(Map<String, List<String>> taskConnections) {
        this.taskConnections = taskConnections;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tasks).append(version).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Stage && equalsSelf((Stage) o);
    }

    private boolean equalsSelf(Stage o) {
        return new EqualsBuilder().append(tasks, o.tasks).append(version, o.version).appendSuper(super.equals(o)).isEquals();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("name", getName())
                .add("version", getVersion())
                .add("tasks", getTasks())
                .toString();
    }
}
