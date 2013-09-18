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
