package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildVariableContributor;
import se.diabol.jenkins.pipeline.PipelineVersionContributor.PipelineVersionAction;

import java.util.Map;

@Extension
public class PipelineVersionBuildVariableContributor extends BuildVariableContributor {
    @Override
    public void buildVariablesFor(AbstractBuild build, Map<String, String> variablesOut) {
        PipelineVersionAction pipelineVersionAction = build.getAction(PipelineVersionAction.class);

        if (pipelineVersionAction != null) {
            variablesOut.put(PipelineVersionContributor.VERSION_PARAMETER, pipelineVersionAction.getVersion());
        }
    }
}