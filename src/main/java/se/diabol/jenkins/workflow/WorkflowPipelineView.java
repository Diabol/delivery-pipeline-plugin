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
package se.diabol.jenkins.workflow;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.model.ViewGroup;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;
import se.diabol.jenkins.workflow.model.Component;
import se.diabol.jenkins.workflow.model.Pipeline;

import javax.servlet.ServletException;

public class WorkflowPipelineView extends View {

    private int noOfColumns = 1;
    public static final int DEFAULT_INTERVAL = 2;

    private int updateInterval = DEFAULT_INTERVAL;

    private String project;
    private transient String error;

    @DataBoundConstructor
    public WorkflowPipelineView(String name) {
        super(name);
    }

    public WorkflowPipelineView(String name, ViewGroup owner) {
        super(name, owner);
    }

    public int getNoOfColumns() {
        return 1;
    }

    public int getUpdateInterval() {
        //This occurs when the plugin has been updated and as long as the view has not been updated
        //Jenkins will set the default value to 0
        if (updateInterval == 0) {
            updateInterval = DEFAULT_INTERVAL;
        }

        return updateInterval;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Exported
    public String getLastUpdated() {
        return PipelineUtils.formatTimestamp(System.currentTimeMillis());
    }

    @Exported
    public String getError() {
        return error;
    }

    @Exported
    public List<Component> getPipelines() {

        try {
            if (project != null) {
                WorkflowJob job = JenkinsUtil.getInstance().getItem(project, JenkinsUtil.getInstance(), WorkflowJob.class);

                List<Pipeline> pipelines = new ArrayList<Pipeline>();

                Iterator<WorkflowRun> it = job.getBuilds().iterator();
                for (int i = 0; i < 3 && it.hasNext(); i++) {
                    WorkflowRun build = it.next();

                    pipelines.add(Pipeline.resolve(job, build));


                }


                Component component = new Component("Component", pipelines);
                this.error = null;
                return Collections.singletonList(component);
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (PipelineException e) {
            error = e.getMessage();
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public Collection<TopLevelItem> getItems() {
        return (Collection) getOwnerItemGroup().getItems();
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return getItems().contains(item);
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if (!isDefault()) {
            return getOwner().getPrimaryView().doCreateItem(req, rsp);
        } else {
            return JenkinsUtil.getInstance().doCreateItem(req, rsp);
        }
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor {

        public ListBoxModel doFillProjectItems(@AncestorInPath ItemGroup<?> context) {
            return ProjectUtil.fillAllProjects(context, WorkflowJob.class);
        }


        @Override
        public String getDisplayName() {
            return "Workflow Pipeline View";
        }

    }
}