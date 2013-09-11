package se.diabol.jenkins.pipeline;

import hudson.model.Descriptor;

public abstract class ComponentComparatorDescriptor extends Descriptor<ComponentComparator> {

    public abstract ComponentComparator createInstance();
}
