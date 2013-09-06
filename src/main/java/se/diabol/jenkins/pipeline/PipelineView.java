package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ViewGroup;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineView extends AbstractPipelineView {

    private String title;
    private String firstJob;
    private int noOfPipelines = 1;

    @DataBoundConstructor
    public PipelineView(String name, String title, String firstJob,int noOfPipelines, ViewGroup owner) {
        super(name, owner);
        this.title = title;
        this.firstJob = firstJob;
        this.noOfPipelines = noOfPipelines;
    }

    @SuppressWarnings("unused")
    public String getTitle() {
        return title;
    }

    @SuppressWarnings("unused")
    public void setTitle(String title) {
        this.title = title;
    }

    @SuppressWarnings("unused")
    public String getFirstJob() {
        return firstJob;
    }

    @SuppressWarnings("unused")
    public void setFirstJob(String firstJob) {
        this.firstJob = firstJob;
    }

    @SuppressWarnings("unused")
    public int getNoOfPipelines() {
        return noOfPipelines;
    }

    @SuppressWarnings("unused")
    public void setNoOfPipelines(int noOfPipelines) {
        this.noOfPipelines = noOfPipelines;
    }

    public List<Pipeline> getPipelines()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        AbstractProject firstJob = Jenkins.getInstance().getItem(this.firstJob, Jenkins.getInstance(), AbstractProject.class);
        return pipelineFactory.createPipelineLatest(pipelineFactory.extractPipeline(getDisplayName(), firstJob), noOfPipelines);
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        if (oldName.equals(firstJob)) {
            firstJob = newName;
        }
    }

    @Extension
    public static class DescriptorImpl extends PipelineViewDescriptor {

        @SuppressWarnings("unused")
        public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
            return ProjectUtil.fillAllProjects(context);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillNoOfPipelinesItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("3", "3");
            options.add("5", "5");
            options.add("10", "10");
            return options;
        }

    }
}
