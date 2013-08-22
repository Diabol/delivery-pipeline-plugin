package com.diabol.pipefitter;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.*;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelinePlugin extends Plugin {

    private static final Logger LOGGER = Logger.getLogger(PipelinePlugin.class.getName());


    public static class PipelineProperty extends JobProperty<AbstractProject<?, ?>> {

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

            @Override
            public PipelineProperty newInstance(StaplerRequest sr, JSONObject formData) throws FormException {
                return new PipelineProperty(sr.getParameter("taskName"),
                        sr.getParameter("stageName"));
            }
        }
    }
}
