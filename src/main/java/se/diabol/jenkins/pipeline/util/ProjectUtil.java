package se.diabol.jenkins.pipeline.util;

import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import se.diabol.jenkins.pipeline.PipelineProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ProjectUtil {

    public static ListBoxModel fillAllProjects(ItemGroup<?> context) {
        ListBoxModel options = new ListBoxModel();
        for (AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            options.add(p.getFullDisplayName(), p.getRelativeNameFrom(context));
        }
        return options;
    }

    public static Set<String> getStageNames() {
        List<AbstractProject> projects =  Jenkins.getInstance().getAllItems(AbstractProject.class);
        Set<String> result = new HashSet<>();
        for (AbstractProject project : projects) {
            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            if (property != null && property.getStageName() != null) {
                result.add(property.getStageName());
            }

        }
        return result;
    }




}
