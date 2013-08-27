package se.diabol.pipefitter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.ViewDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class MultiPipelineView extends AbstractPipelineView {

    private List<Component> components;

    @DataBoundConstructor
    public MultiPipelineView(String name,List<Component> components ) {
        super(name);
        this.components = components;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor {
        public String getDisplayName() {
            return "Pipeline View";
        }

    }


    public static class Component extends AbstractDescribableImpl<Component> {
        public String name;

        @DataBoundConstructor
        public Component(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<Component> {
            @Override
            public String getDisplayName() {
                return "";
            }



        }
    }
}
