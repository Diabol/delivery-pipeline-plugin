package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.*;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import se.diabol.jenkins.pipeline.model.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class MultiPipelineView extends AbstractPipelineView {

    private List<Component> components;

    @DataBoundConstructor
    public MultiPipelineView(String name, List<Component> components ) {
        super(name);
        this.components = components;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        for (Component component : components) {
            if (component.getFirstJob().equals(oldName)) {
                component.setFirstJob(newName);
            }
        }
    }

    public List<Pipeline> getPipelines()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        List<Pipeline> result = new ArrayList<>();
        for (Component component : components) {
            AbstractProject firstJob = Jenkins.getInstance().getItem(component.getFirstJob(), Jenkins.getInstance(), AbstractProject.class);

            result.add(pipelineFactory.createPipelineLatest(pipelineFactory.extractPipeline(component.getName(), firstJob)));

        }
        return result;
    }


    @Extension
    public static class DescriptorImpl extends ViewDescriptor {
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
                final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
                for (final AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                    options.add(p.getFullDisplayName(), p.getRelativeNameFrom(context));
                }
                return options;
            }



        }
    }
}
