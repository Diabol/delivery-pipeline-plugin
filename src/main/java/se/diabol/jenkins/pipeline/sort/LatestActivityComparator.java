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
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.Pipeline;
import se.diabol.jenkins.pipeline.domain.Stage;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.io.Serializable;

public class LatestActivityComparator extends ComponentComparator implements Serializable {

    @Override
    public int compare(Component o1, Component o2) {
        return Long.valueOf(getLastActivity(o2)).compareTo(getLastActivity(o1));
    }

    private long getLastActivity(Pipeline pipeline) {
        long result = 0;
        for (Stage stage : pipeline.getStages()) {
            for (Task task : stage.getTasks()) {
                if (task.getStatus().getLastActivity() > result) {
                    result = task.getStatus().getLastActivity();
                }
            }
        }
        return result;
    }

    private long getLastActivity(Component component) {
        long result = 0;
        for (Pipeline pipeline : component.getPipelines()) {
            long lastActivity = getLastActivity(pipeline);
            if (lastActivity > result) {
                result = lastActivity;
            }
        }
        return result;
    }

    @Extension
    public static class DescriptorImpl extends ComponentComparatorDescriptor {
        @Override
        public String getDisplayName() {
            return "Sorting by last activity";
        }

        @Override
        public ComponentComparator createInstance() {
            return new LatestActivityComparator();
        }
    }


}
