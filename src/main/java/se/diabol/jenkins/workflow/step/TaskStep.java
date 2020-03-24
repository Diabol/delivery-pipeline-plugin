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
package se.diabol.jenkins.workflow.step;

import hudson.Extension;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class TaskStep extends AbstractStepImpl {

    private final String name;
    private String description;

    @DataBoundConstructor
    public TaskStep(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("must specify name");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(TaskStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "task";
        }

        @Override
        public String getDisplayName() {
            return "Task";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }
}
