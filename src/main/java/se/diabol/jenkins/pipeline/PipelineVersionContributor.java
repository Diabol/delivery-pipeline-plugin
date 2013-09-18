package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class PipelineVersionContributor extends RunListener<Run> {

    private static final Logger LOG = Logger.getLogger(PipelineVersionContributor.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
        if (run instanceof AbstractBuild) {
            try {
                AbstractBuild build = (AbstractBuild) run;
                PipelineVersionProperty property = (PipelineVersionProperty) build.getProject().getProperty(PipelineVersionProperty.class);
                if (property != null && property.getCreateVersion()) {

                    String version = TokenMacro.expand(build, listener, property.getVersionTemplate());
                    ParametersAction action = new ParametersAction(new StringParameterValue("PIPELINE_VERSION", version));
                    build.addAction(action);

                    listener.getLogger().println("Creating version: " + version);

                    if (property.getUpdateDisplayName()) {
                        build.setDisplayName(version);
                    }


                } else {
                    AbstractBuild upstreamBuild = PipelineFactory.getUpstreamBuild(build);
                    if (upstreamBuild != null) {
                        List<ParametersAction> parameters = upstreamBuild.getActions(ParametersAction.class);
                        for (ParametersAction parameter : parameters) {
                            ParameterValue value = parameter.getParameter("PIPELINE_VERSION");
                            if (value != null && value instanceof StringParameterValue) {
                                String version = ((StringParameterValue) value).value;
                                ParametersAction action = new ParametersAction(new StringParameterValue("PIPELINE_VERSION", version));
                                build.addAction(action);

                                listener.getLogger().println("Setting version to: " + version + " from upstream version");

                            }
                        }
                    }
                }
            } catch (MacroEvaluationException | InterruptedException | IOException e) {
                LOG.log(Level.WARNING, "Error creating version", e);
            }


        }

    }


}
