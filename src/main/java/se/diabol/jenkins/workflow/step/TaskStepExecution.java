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

import com.google.common.util.concurrent.FutureCallback;
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

import java.io.Serializable;
import java.util.logging.Logger;

public class TaskStepExecution extends AbstractStepExecutionImpl {
    
    private static final long serialVersionUID = 1661891433860818355L;

    private static final Logger LOG = Logger.getLogger(TaskStepExecution.class.getName());

    @StepContextParameter
    private transient FlowNode node;

    @Inject(optional = true)
    private transient TaskStep step;

    @Override
    public boolean start() throws Exception {
        TaskAction taskAction = new TaskActionImpl(node, step.name);
        StepContext context = this.getContext();
        if (context.hasBody()) {
            ((CpsBodyInvoker) context.newBodyInvoker())
                    .withStartAction(taskAction)
                    .withCallback(new TaskBodyExecutionWrapper(taskAction, context))
                    .withDisplayName(step.name).start();
        } else {
            LOG.warning("Task pipeline step without body is deprecated and "
                    + "may be subject for removal in a future release");
            node.addAction(taskAction);
            getContext().onSuccess(null);
        }
        node.addAction(new LabelAction(step.name));
        if (node.getAction(TimingAction.class) == null) {
            node.addAction(new TimingAction());
        }
        return false;
    }

    @Override
    public void stop(Throwable throwable) throws Exception {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private static final class TaskActionImpl extends InvisibleAction implements TaskAction, Serializable {

        private static final long serialVersionUID = -7007957945134870234L;
        private final transient FlowNode associatedNode;
        private final String taskName;
        private Long finishedTime = null;

        TaskActionImpl(FlowNode associatedNode, String taskName) {
            this(associatedNode, taskName, null);
        }

        TaskActionImpl(FlowNode associatedNode, String taskName, Long finishedTime) {
            this.associatedNode = associatedNode;
            this.taskName = taskName;
            this.finishedTime = finishedTime;
        }

        @Override
        public String getTaskName() {
            return taskName;
        }

        @Override
        public Long getFinishedTime() {
            return finishedTime;
        }

        @Override
        public void setFinishedTime(Long finishedTime) {
            this.finishedTime = finishedTime;
            this.associatedNode.addAction(new TaskFinishedAction(finishedTime));
        }
    }

    private static class TaskBodyExecutionWrapper extends BodyExecutionCallback {
        private final FutureCallback<Object> futureCallback;
        private TaskAction taskAction;

        TaskBodyExecutionWrapper(TaskAction taskAction, FutureCallback<Object> futureCallback) {
            if (!(futureCallback instanceof Serializable)) {
                throw new IllegalArgumentException(futureCallback.getClass() + " is not serializable");
            }
            if (taskAction == null || !(taskAction instanceof Serializable)) {
                throw new IllegalArgumentException("Task execution expects serializable task action");
            }
            this.futureCallback = futureCallback;
            this.taskAction = taskAction;
        }

        @Override
        public void onSuccess(StepContext context, Object result) {
            taskAction.setFinishedTime(System.currentTimeMillis());
            futureCallback.onSuccess(result);
        }

        @Override
        public void onFailure(StepContext context, Throwable throwable) {
            futureCallback.onFailure(throwable);
        }

        private static final long serialVersionUID = 1L;
    }
}
