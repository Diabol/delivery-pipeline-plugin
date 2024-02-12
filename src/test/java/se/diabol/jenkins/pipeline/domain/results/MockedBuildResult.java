package se.diabol.jenkins.pipeline.domain.results;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;

public class MockedBuildResult extends BuildResult {

    protected MockedBuildResult(AbstractBuild<?, ?> build, BuildHistory history, ParserResult result, String defaultEncoding) {
        super(build, history, result, defaultEncoding);
    }

    @Override
    protected String getSerializationFileName() {
        return null;
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return null;
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
