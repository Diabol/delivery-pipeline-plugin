package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptySet;

@SuppressWarnings("UnusedDeclaration")
public class DeliveryPipelineView extends View {

    private List<Component> components;
    private int noOfPipelines = 1;
    private boolean showAggregatedPipeline = false;
    private int noOfColumns = 1;

    @DataBoundConstructor
    public DeliveryPipelineView(String name, int noOfColumns, List<Component> components,
                                int noOfPipelines, boolean showAggregatedPipeline) {
        super(name);
        this.components = components;
        this.noOfColumns = noOfColumns;
        this.noOfPipelines = noOfPipelines;
        this.showAggregatedPipeline = showAggregatedPipeline;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public int getNoOfPipelines() {
        return noOfPipelines;
    }

    public boolean isShowAggregatedPipeline() {
        return showAggregatedPipeline;
    }

    public void setNoOfPipelines(int noOfPipelines) {
        this.noOfPipelines = noOfPipelines;
    }

    public void setShowAggregatedPipeline(boolean showAggregatedPipeline) {
        this.showAggregatedPipeline = showAggregatedPipeline;
    }

    public int getNoOfColumns() {
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        for (Component component : components) {
            if (component.getFirstJob().equals(oldName)) {
                component.setFirstJob(newName);
            }
        }
    }

    @Exported
    public List<Pipeline> getPipelines()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        List<Pipeline> result = new ArrayList<>();
        for (Component component : components) {
            Jenkins jenkins = Jenkins.getInstance();
            AbstractProject firstJob = jenkins.getItem(component.getFirstJob(), jenkins, AbstractProject.class);
            Pipeline prototype = pipelineFactory.extractPipeline(component.getName(), firstJob);
            if(showAggregatedPipeline)
                result.add(pipelineFactory.createPipelineAggregated(prototype));
            result.addAll(pipelineFactory.createPipelineLatest(prototype, noOfPipelines));
        }
        return result;
    }

    public String getRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return emptySet(); // Not using the getItems functionality.
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return false;
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return getOwner().getPrimaryView().doCreateItem(req, rsp);
    }


    @Extension
    public static class DescriptorImpl extends ViewDescriptor
    {
        public ListBoxModel doFillNoOfColumnsItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }
        public ListBoxModel doFillNoOfPipelinesItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            for(int i = 0; i <= 10; i++) {
                String opt = String.valueOf(i);
                options.add(opt, opt);
            }
            return options;
        }

        @Override
        public String getDisplayName() {
            return "Delivery Pipeline View";
        }
    }


    public static class Component extends AbstractDescribableImpl<Component> {
        private String name;
        private String firstJob;

        @DataBoundConstructor
        public Component(String name, String firstJob) {
            this.name = name;
            this.firstJob = firstJob;
        }

        public String getName() {
            return name;
        }

        public String getFirstJob() {
            return firstJob;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFirstJob(String firstJob) {
            this.firstJob = firstJob;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<Component> {
            @Override
            public String getDisplayName() {
                return "";
            }

            public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
                return ProjectUtil.fillAllProjects(context);
            }

            public FormValidation doCheckName(@QueryParameter String value) {
                if (value != null && !value.trim().equals("")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Please supply a title!");
                }
            }

        }
    }
}
