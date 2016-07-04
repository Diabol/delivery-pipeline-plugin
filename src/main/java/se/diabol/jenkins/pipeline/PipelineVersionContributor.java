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
package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.CauseAction;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

public class PipelineVersionContributor extends BuildWrapper {

    public static final String VERSION_PARAMETER = "PIPELINE_VERSION";

    private final String versionTemplate;
    private boolean updateDisplayName = false;

    private static final Logger LOG = Logger.getLogger(PipelineVersionContributor.class.getName());

    @DataBoundConstructor
    public PipelineVersionContributor(boolean updateDisplayName, String versionTemplate) {
        this.updateDisplayName = updateDisplayName;
        this.versionTemplate = versionTemplate;
    }

    public String getVersionTemplate() {
        return versionTemplate;
    }

    public boolean isUpdateDisplayName() {
        return updateDisplayName;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
            InterruptedException {
        try {

            String version = TokenMacro.expandAll(build, listener, getVersionTemplate());
            setVersion(build, version);
            listener.getLogger().println("Creating version: " + version);

            if (isUpdateDisplayName()) {
                build.setDisplayName(version);
            }

        } catch (MacroEvaluationException e) {
            listener.getLogger().println("Error creating version: " + e.getMessage());
            LOG.log(Level.WARNING, "Error creating version", e);
        }
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener)
                    throws IOException, InterruptedException {
                return true;
            }
        };
    }

    @CheckForNull
    public static String getVersion(AbstractBuild build)  {
        PipelineVersionAction action = build.getAction(PipelineVersionAction.class);
        if (action != null) {
            return action.getVersion();
        }
        return null;
    }

    static void setVersion(AbstractBuild build, String version) {
        PipelineVersionAction action = build.getAction(PipelineVersionAction.class);
        if (action == null) {
            build.addAction(new PipelineVersionAction(version));
        } else {
            build.replaceAction(action);
        }
        build.replaceAction(getVersionParameterAction(build, version));
    }

    // Backwards compatibility for 0.9.9 and older
    private static ParametersAction getVersionParameterAction(AbstractBuild build, String version) {
        ParameterValue value = new StringParameterValue(PipelineVersionContributor.VERSION_PARAMETER, version);
        ParametersAction action = build.getAction(ParametersAction.class);
        if (action != null) {
            List<ParameterValue> parameters = new ArrayList<ParameterValue>(action.getParameters());
            parameters.add(value);
            return new ParametersAction(parameters);
        }
        return new ParametersAction(value);
    }

    static class PipelineVersionAction extends CauseAction {
        private final String version;

        PipelineVersionAction(final String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Create Delivery Pipeline version";
        }
    }

}
