package se.diabol.jenkins.pipeline.sort;

import hudson.Extension;
import se.diabol.jenkins.pipeline.model.Component;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;

public class LatestActivityComparator extends ComponentComparator {

    @Override
    public int compare(Component o1, Component o2) {
        return Long.compare(getLastActivity(o2), getLastActivity(o1));
    }

    private long getLastActivity(Pipeline pipeline) {
        long result = 0;
        for (Stage stage: pipeline.getStages()) {
            for (Task task: stage.getTasks()) {
                if (task.getStatus().getLastActivity() > result) {
                    result = task.getStatus().getLastActivity();
                }
            }
        }
        return result;
    }

    private long getLastActivity(Component component) {
        long result = 0;
        for (Pipeline pipeline: component.getPipelines()) {
            long lastActivity = getLastActivity(pipeline);
            if (lastActivity > result) {
                result = lastActivity;
            }
        }
        return result;
    }

    @Extension
    public static class DescriptorImpl extends ComponentComparatorDescriptor
    {
        @Override
        public String getDisplayName() {
            return "Sorting by last activity";
        }

        @Override
        public ComponentComparator createInstance() {
            return new LatestActivityComparator();
        }
    }


}
