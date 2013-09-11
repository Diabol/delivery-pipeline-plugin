package se.diabol.jenkins.pipeline;

import hudson.Extension;
import se.diabol.jenkins.pipeline.model.Component;

public class NameComparator extends ComponentComparator {

    @Override
    public int compare(Component o1, Component o2) {
        return o1.getName().compareTo(o2.getName());
    }

    @Extension
    public static class DescriptorImpl extends ComponentComparatorDescriptor
    {
        @Override
        public String getDisplayName() {
            return "Sort by title";
        }

        @Override
        public ComponentComparator createInstance() {
            return new LatestActivityComparator();
        }
    }


}
