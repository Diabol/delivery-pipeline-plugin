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

import static com.google.common.base.Objects.toStringHelper;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Stage extends AbstractItem {
    private List<Task> tasks;

    private String version;

    public Stage(String name, List<Task> tasks, String version) {
        super(name);
        this.tasks = tasks;
        this.version = version;
    }

    public Stage(String name, List<Task> tasks) {
        super(name);
        this.tasks = ImmutableList.copyOf(tasks);
    }

    @Exported
    public List<Task> getTasks() {
        return tasks;
    }

    @Exported
    public String getVersion() {
        return version;
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
