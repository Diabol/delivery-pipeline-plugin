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
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class PipelineVersionProperty extends JobProperty<AbstractProject<?, ?>> {

    private String versionTemplate;
    private boolean createVersion = false;
    private boolean updateDisplayName = false;

    @DataBoundConstructor
    public PipelineVersionProperty(boolean updateDisplayName, boolean createVersion, String versionTemplate) {
        this.updateDisplayName = updateDisplayName;
        this.createVersion = createVersion;
        this.versionTemplate = versionTemplate;
    }

    @Exported
    public String getVersionTemplate() {
        return versionTemplate;
    }

    @Exported
    public boolean getCreateVersion() {
        return createVersion;
    }

    @Exported
    public boolean getUpdateDisplayName() {
        return updateDisplayName;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {
        public String getDisplayName() {
            return "Pipeline version";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

    }


}
