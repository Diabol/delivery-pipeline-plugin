package se.diabol.jenkins.pipeline.domain.results;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.PluginDescriptor;

public class MockedResultAction extends AbstractResultAction<MockedBuildResult>  {

    public MockedResultAction(AbstractBuild<?, ?> owner, AbstractHealthDescriptor healthDescriptor, MockedBuildResult result) {
        super(owner, healthDescriptor, result);
    }

    @Override
    protected PluginDescriptor getDescriptor() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
