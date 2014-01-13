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
import hudson.model.*;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

public class PipelineVersionContributor extends BuildWrapper {

    public static final String VERSION_PARAMETER = "PIPELINE_VERSION";

    private String versionTemplate;
    private boolean updateDisplayName = false;

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
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        onStarted(build, listener);
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                return true;
            }
        };
    }

    public void onStarted(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        try {

            String version = TokenMacro.expand(build, listener, getVersionTemplate());
            setVersion(build, version);
            listener.getLogger().println("Creating version: " + version);

            if (isUpdateDisplayName()) {
                build.setDisplayName(version);
            }

        } catch (MacroEvaluationException e) {
            listener.getLogger().println("Error creating version: " + e.getMessage());
        }
    }

    public static String getVersion(AbstractBuild build) {
        PipelineVersionAction version = build.getAction(PipelineVersionAction.class);
        if (version != null) {
            return version.getVersion();
        }
        //Old way for backwards compatibility
        List<ParametersAction> parameters = build.getActions(ParametersAction.class);
        for (ParametersAction parameter : parameters) {
            ParameterValue value = parameter.getParameter(PipelineVersionContributor.VERSION_PARAMETER);
            if (value instanceof StringParameterValue) {
                return ((StringParameterValue) value).value;
            }
        }

        return null;
    }

    public static void setVersion(AbstractBuild build, String version) throws IOException {

        PipelineVersionAction versionAction = build.getAction(PipelineVersionAction.class);
        if (versionAction != null) {
            versionAction.setVersion(version);
        } else {
            build.addAction(new PipelineVersionAction(version));
        }
        build.save();

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
