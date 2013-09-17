package se.diabol.jenkins.pipeline.sort;

import hudson.Extension;
import se.diabol.jenkins.pipeline.model.Component;

public class NoOpComparator extends ComponentComparator {

    @Override
    public int compare(Component o1, Component o2) {
        return 0;
    }

    @Extension
    public static class DescriptorImpl extends ComponentComparatorDescriptor
    {
        @Override
        public String getDisplayName() {
            return "No sorting";
        }

        @Override
        public ComponentComparator createInstance() {
            return new LatestActivityComparator();
        }
    }

}
