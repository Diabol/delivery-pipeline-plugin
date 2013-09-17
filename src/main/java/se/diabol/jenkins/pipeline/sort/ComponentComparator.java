package se.diabol.jenkins.pipeline.sort;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import se.diabol.jenkins.pipeline.model.Component;

import java.util.Comparator;

public abstract class ComponentComparator implements Comparator<Component>, ExtensionPoint, Describable<ComponentComparator> {

    @Override
    public Descriptor<ComponentComparator> getDescriptor() {
        return (ComponentComparatorDescriptor) Jenkins.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<ComponentComparator,ComponentComparatorDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(ComponentComparator.class);
    }
}
