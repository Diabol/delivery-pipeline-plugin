package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.Set;

public class PipelineProperty extends JobProperty<AbstractProject<?, ?>> {

    private String taskName;
    private String stageName;

    @DataBoundConstructor
    public PipelineProperty(String taskName, String stageName) {
        this.taskName = taskName;
        this.stageName = stageName;
    }

    @Exported
    public String getTaskName() {
        return taskName;
    }

    @Exported
    public String getStageName() {
        return stageName;
    }


    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {
        public String getDisplayName() {
            return "Pipeline description";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

        @SuppressWarnings("unused")
        public AutoCompletionCandidates doAutoCompleteStageName(@QueryParameter String value) {
            if (value != null) {
                AutoCompletionCandidates c = new AutoCompletionCandidates();
                Set<String> stages = ProjectUtil.getStageNames();

                for (String stage : stages)
                    if (stage.toLowerCase().startsWith(value.toLowerCase()))
                        c.add(stage);
                return c;
            } else {
                return new AutoCompletionCandidates();
            }
        }

        @Override
        public PipelineProperty newInstance(StaplerRequest sr, JSONObject formData) throws FormException {
            return new PipelineProperty(sr.getParameter("taskName"),
                    sr.getParameter("stageName"));
        }
    }
}
