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

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.collect.ImmutableList;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.core.AbstractItem;
import se.diabol.jenkins.core.GenericComponent;

import java.util.Collections;
import java.util.List;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Component extends GenericComponent {
    private final List<Pipeline> pipelines;
    private final WorkflowJob workflowJob;

    public Component(String name, WorkflowJob job, List<Pipeline> pipelines) {
        super(name);
        this.workflowJob = job;
        if (pipelines != null) {
            this.pipelines = ImmutableList.copyOf(pipelines);
        } else {
            this.pipelines = Collections.emptyList();
        }
    }

    @Exported
    public boolean isWorkflowComponent() {
        return true;
    }

    @Exported
    public String getWorkflowUrl() {
        return workflowJob.getUrl();
    }

    @Exported
    public WorkflowJob getWorkflowJob() {
        return workflowJob;
    }

    @Exported
    public String getFullJobName() {
        return workflowJob.getFullName();
    }

    @Exported
    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("name", getName()).add("pipelines", pipelines).toString();
    }
}
