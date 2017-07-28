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

import com.google.inject.Inject;
import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.CpsBodyInvoker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

public class TaskStepExecution extends AbstractStepExecutionImpl {

    @StepContextParameter
    private transient FlowNode node;

    @Inject(optional = true)
    private transient TaskStep step;

    @Override
    public boolean start() throws Exception {
        TaskAction taskAction = new TaskActionImpl(step.name);
        StepContext context = this.getContext();
        if (context.hasBody()) {
            ((CpsBodyInvoker) context.newBodyInvoker())
                    .withContext(context)
                    .withStartAction(taskAction)
                    .withCallback(BodyExecutionCallback.wrap(context))
                    .withDisplayName(step.name).start();
        } else {
            node.addAction(taskAction);
        }
        node.addAction(new LabelAction(step.name));
        if (node.getAction(TimingAction.class) == null) {
            node.addAction(new TimingAction());
        }
        return false;
    }

    @Override
    public void stop(Throwable throwable) throws Exception {
        getContext().onFailure(throwable);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private static final class TaskActionImpl extends InvisibleAction implements TaskAction {
        private final String taskName;

        TaskActionImpl(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public String getTaskName() {
            return taskName;
        }
    }
}
