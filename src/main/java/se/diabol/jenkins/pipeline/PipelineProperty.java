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
import hudson.model.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PipelineProperty extends JobProperty<AbstractProject<?, ?>> {

    private String taskName = null;
    private String stageName = null;
    private String descriptionTemplate = null;

    public PipelineProperty() {
    }

    @DataBoundConstructor
    public PipelineProperty(String taskName, String stageName, String descriptionTemplate) {
        setStageName(stageName);
        setTaskName(taskName);
        setDescriptionTemplate(descriptionTemplate);
    }

    @Exported
    public String getTaskName() {
        return taskName;
    }

    @Exported
    public String getStageName() {
        return stageName;
    }

    @Exported
    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public final void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public final void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public static Set<String> getStageNames() {
        List<AbstractProject> projects = JenkinsUtil.getInstance().getAllItems(AbstractProject.class);
        Set<String> result = new HashSet<String>();
        for (AbstractProject project : projects) {
            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            if (property != null && property.getStageName() != null) {
                result.add(property.getStageName());
            }

        }
        return result;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "Pipeline description";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

        public AutoCompletionCandidates doAutoCompleteStageName(@QueryParameter String value) {
            if (value != null) {
                AutoCompletionCandidates c = new AutoCompletionCandidates();
                Set<String> stages = getStageNames();

                for (String stage : stages) {
                    if (stage.toLowerCase().startsWith(value.toLowerCase())) {
                        c.add(stage);
                    }
                }
                return c;
            } else {
                return new AutoCompletionCandidates();
            }
        }

        public FormValidation doCheckStageName(@QueryParameter String value) {
            return checkValue(value);
        }

        public FormValidation doCheckTaskName(@QueryParameter String value) {
            return checkValue(value);
        }

        protected FormValidation checkValue(String value) {
            if (value == null || "".equals(value)) {
                return FormValidation.ok();
            }
            if ("".equals(value.trim())) {
                return FormValidation.error("Value needs to be empty or include characters and/or numbers");
            }
            return FormValidation.ok();
        }

        @Override
        public PipelineProperty newInstance(StaplerRequest sr, JSONObject formData) throws FormException {
            String task = sr.getParameter("taskName");
            String stage = sr.getParameter("stageName");
            String description = sr.getParameter("descriptionTemplate");
            boolean configEnabled = sr.getParameter("enabled") != null;
            if (!configEnabled) {
                return null;
            }
            if ("".equals(task)) {
                task = null;
            }
            if ("".equals(stage)) {
                stage = null;
            }
            if ("".equals(description)) {
                description = null;
            }
            if (task == null && stage == null) {
                return null;
            }
            return new PipelineProperty(task, stage, description);
        }
    }
}
