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
package se.diabol.jenkins.pipeline.sort;

import hudson.Extension;
import se.diabol.jenkins.core.GenericComponent;
import se.diabol.jenkins.core.GenericPipeline;
import se.diabol.jenkins.pipeline.domain.Pipeline;
import se.diabol.jenkins.pipeline.domain.Stage;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.io.Serializable;

public class FailedJobComparator extends GenericComponentComparator implements Serializable {

    @Override
    public int compare(GenericComponent o1, GenericComponent o2) {
        if ((hasFailedJob(firstPipeline(o1)) && (!hasFailedJob(firstPipeline(o2))))) {
            return -1;
        } else if ((hasFailedJob(firstPipeline(o2)) && (!hasFailedJob(firstPipeline(o1))))) {
            return 1;
        } else {
            return new LatestActivityComparator().compare(o1, o2);
        }
    }

    private GenericPipeline firstPipeline(GenericComponent component) {
        if (component != null && component.getPipelines() != null && !component.getPipelines().isEmpty()) {
            return component.getPipelines().get(0);
        } else {
            return null;
        }
    }

    private boolean hasFailedJob(GenericPipeline pipeline) {
        if (pipeline == null) {
            return false;
        }
        if (pipeline instanceof Pipeline) {
            return hasFailedJobs((Pipeline) pipeline);
        } else if (pipeline instanceof se.diabol.jenkins.workflow.model.Pipeline) {
            return hasFailed((se.diabol.jenkins.workflow.model.Pipeline) pipeline);
        } else {
            throw new IllegalStateException("Unable to resolve pipeline type for " + pipeline);
        }
    }

    private boolean hasFailedJobs(Pipeline pipeline) {
        if (pipeline != null) {
            for (Stage stage : pipeline.getStages()) {
                for (Task task : stage.getTasks()) {
                    if (task.getStatus().isFailed()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasFailed(se.diabol.jenkins.workflow.model.Pipeline pipeline) {
        if (pipeline != null) {
            for (se.diabol.jenkins.workflow.model.Stage stage : pipeline.getStages()) {
                for (se.diabol.jenkins.workflow.model.Task task : stage.getTasks()) {
                    if (task.getStatus().isFailed()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Extension
    public static class DescriptorImpl extends ComponentComparatorDescriptor {
        @Override
        public String getDisplayName() {
            return "Sorting by failed pipelines, then by last activity";
        }

        @Override
        public GenericComponentComparator createInstance() {
            return new FailedJobComparator();
        }
    }


}
