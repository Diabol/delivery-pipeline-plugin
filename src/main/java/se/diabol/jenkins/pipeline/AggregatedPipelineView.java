package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import se.diabol.jenkins.pipeline.model.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class AggregatedPipelineView extends MultiPipelineView {

    @DataBoundConstructor
    public AggregatedPipelineView(String name, int noOfColumns, List<Component> components) {
        super(name, noOfColumns, components);
    }

    public List<Pipeline> getPipelines()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        List<Pipeline> result = new ArrayList<>();
        for (Component component : getComponents()) {
            AbstractProject firstJob = Jenkins.getInstance().getItem(component.getFirstJob(), Jenkins.getInstance(), AbstractProject.class);

            result.add(pipelineFactory.createPipelineAggregated(pipelineFactory.extractPipeline(component.getName(), firstJob)));

        }
        return result;
    }


    @Extension
    public static class DescriptorImpl extends MultiPipelineView.DescriptorImpl {

    }


}
