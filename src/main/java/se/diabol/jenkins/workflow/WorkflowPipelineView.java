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
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.domain.Change;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;
import se.diabol.jenkins.workflow.model.Component;
import se.diabol.jenkins.workflow.model.Pipeline;

import javax.servlet.ServletException;

import static se.diabol.jenkins.pipeline.DeliveryPipelineView.DEFAULT_THEME;

public class WorkflowPipelineView extends View {

    public static final int DEFAULT_INTERVAL = 2;

    public static final int DEFAULT_NO_OF_PIPELINES = 3;
    private static final int MAX_NO_OF_PIPELINES = 50;

    private int updateInterval = DEFAULT_INTERVAL;
    private int noOfPipelines = DEFAULT_NO_OF_PIPELINES;
    private int noOfColumns = 1;
    private boolean allowPipelineStart = false;
    private boolean showChanges = false;
    private String theme = DEFAULT_THEME;
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
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    public int getUpdateInterval() {
        //This occurs when the plugin has been updated and as long as the view has not been updated
        //Jenkins will set the default value to 0
        if (updateInterval == 0) {
            updateInterval = DEFAULT_INTERVAL;
        }

        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public int getNoOfPipelines() {
        return noOfPipelines;
    }

    public void setNoOfPipelines(int noOfPipelines) {
        this.noOfPipelines = noOfPipelines;
    }

    @Exported
    public boolean isAllowPipelineStart() {
        return allowPipelineStart;
    }

    public void setAllowPipelineStart(boolean allowPipelineStart) {
        this.allowPipelineStart = allowPipelineStart;
    }

    public boolean isShowChanges() {
        return showChanges;
    }

    public void setShowChanges(boolean showChanges) {
        this.showChanges = showChanges;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTheme() {
        return this.theme == null ? DEFAULT_THEME : this.theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
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
                WorkflowJob job = getWorkflowJob(project);
                List<Pipeline> pipelines = resolvePipelines(job);
                Component component = new Component(job.getName(), job, pipelines);
                this.error = null;
                return Collections.singletonList(component);
            } else {
                return Collections.emptyList();
            }
        } catch (PipelineException e) {
            error = e.getMessage();
            return Collections.emptyList();
        }
    }

    private List<Pipeline> resolvePipelines(WorkflowJob job) throws PipelineException {
        List<Pipeline> pipelines = new ArrayList<Pipeline>();

        Iterator<WorkflowRun> it = job.getBuilds().iterator();
        for (int i = 0; i < noOfPipelines && it.hasNext(); i++) {
            WorkflowRun build = it.next();
            Pipeline pipeline = resolvePipeline(job, build);
            pipelines.add(pipeline);
        }
        return pipelines;
    }

    private Pipeline resolvePipeline(WorkflowJob job, WorkflowRun build) throws PipelineException {
        Pipeline pipeline = Pipeline.resolve(job, build);
        if (showChanges) {
            pipeline.setChanges(getChangelog(build));
        }
        return pipeline;
    }

    private WorkflowJob getWorkflowJob(final String projectName) {
        return jenkins().getItem(projectName, jenkins(), WorkflowJob.class);
    }

    private List<Change> getChangelog(WorkflowRun build) {
        return Change.getChanges(build.getChangeSets());
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
            return jenkins().doCreateItem(req, rsp);
        }
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor {
        public ListBoxModel doFillNoOfColumnsItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }

        public ListBoxModel doFillProjectItems(@AncestorInPath ItemGroup<?> context) {
            return ProjectUtil.fillAllProjects(context, WorkflowJob.class);
        }

        public ListBoxModel doFillNoOfPipelinesItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            for (int i = 1; i <= MAX_NO_OF_PIPELINES; i++) {
                String opt = String.valueOf(i);
                options.add(opt, opt);
            }
            return options;
        }

        public FormValidation doCheckUpdateInterval(@QueryParameter String value) {
            int valueAsInt;
            try {
                valueAsInt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error(e, "Value must be an integer");
            }
            if (valueAsInt <= 0) {
                return FormValidation.error("Value must be greater than 0");
            }
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Delivery Pipeline View for Pipeline/Workflow plugin";
        }
    }

    private static Jenkins jenkins() {
        return JenkinsUtil.getInstance();
    }

}