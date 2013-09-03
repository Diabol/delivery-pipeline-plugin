package se.diabol.jenkins.pipeline;

import hudson.model.ViewDescriptor;

public class PipelineViewDescriptor extends ViewDescriptor {

    @Override
    public String getDisplayName() {
        return "Delivery Pipeline View";
    }


}
