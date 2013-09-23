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

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.model.Component;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Task;
import se.diabol.jenkins.pipeline.model.status.StatusFactory;
import se.diabol.jenkins.pipeline.sort.ComponentComparator;
import se.diabol.jenkins.pipeline.sort.ComponentComparatorDescriptor;
import se.diabol.jenkins.pipeline.sort.NoOpComparator;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class DeliveryPipelineView extends View {

    private List<ComponentSpec> componentSpecs;
    private int noOfPipelines = 1;
    private boolean showAggregatedPipeline = false;
    private int noOfColumns = 1;
    private String sorting = NoOpComparator.class.getName();
    private String fullScreenCss = null;
    private String embeddedCss = null;

    @DataBoundConstructor
    public DeliveryPipelineView(String name, int noOfColumns, List<ComponentSpec> componentSpecs,
                                int noOfPipelines, boolean showAggregatedPipeline) {
        super(name);
        this.componentSpecs = componentSpecs;
        this.noOfColumns = noOfColumns;
        this.noOfPipelines = noOfPipelines;
        this.showAggregatedPipeline = showAggregatedPipeline;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public List<ComponentSpec> getComponentSpecs() {
        return componentSpecs;
    }

    public void setComponentSpecs(List<ComponentSpec> componentSpecs) {
        this.componentSpecs = componentSpecs;
    }

    public int getNoOfPipelines() {
        return noOfPipelines;
    }

    public boolean isShowAggregatedPipeline() {
        return showAggregatedPipeline;
    }

    public void setNoOfPipelines(int noOfPipelines) {
        this.noOfPipelines = noOfPipelines;
    }

    public void setShowAggregatedPipeline(boolean showAggregatedPipeline) {
        this.showAggregatedPipeline = showAggregatedPipeline;
    }

    public int getNoOfColumns() {
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    public String getFullScreenCss() {
        return fullScreenCss;
    }

    public void setFullScreenCss(String fullScreenCss) {
        if (fullScreenCss != null && fullScreenCss.trim().equals("")) {
            this.fullScreenCss = null;
        } else {
            this.fullScreenCss = fullScreenCss;
        }
    }

    public String getEmbeddedCss() {
        return embeddedCss;
    }

    public void setEmbeddedCss(String embeddedCss) {
        if (embeddedCss != null && embeddedCss.trim().equals("")) {
            this.embeddedCss = null;
        } else {
            this.embeddedCss = embeddedCss;
        }
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        Iterator<ComponentSpec> it = componentSpecs.iterator();
        while (it.hasNext()) {
            ComponentSpec componentSpec = it.next();
            if (componentSpec.getFirstJob().equals(oldName)) {
                if (newName == null) {
                    it.remove();
                } else {
                    componentSpec.setFirstJob(newName);
                }
            }
        }
    }

    @Exported
    public List<Component> getPipelines()
    {
        List<Component> components = new ArrayList<>();
        for (ComponentSpec componentSpec : componentSpecs) {
            Jenkins jenkins = Jenkins.getInstance();
            AbstractProject firstJob = jenkins.getItem(componentSpec.getFirstJob(), jenkins, AbstractProject.class);
            Pipeline prototype = PipelineFactory.extractPipeline(componentSpec.getName(), firstJob);
            List<Pipeline> pipelines = new ArrayList<>();
            if(showAggregatedPipeline)
                pipelines.add(PipelineFactory.createPipelineAggregated(prototype));
            pipelines.addAll(PipelineFactory.createPipelineLatest(prototype, noOfPipelines));
            components.add(new Component(componentSpec.getName(), pipelines));
        }
        if (sorting != null) {
            ComponentComparatorDescriptor comparatorDescriptor = ComponentComparator.all().find(sorting);
            if (comparatorDescriptor != null) {
                Collections.sort(components, comparatorDescriptor.createInstance());
            }
        }

        return components;
    }

    @JavaScriptMethod
    public Task getTask(String id, int build) {
        System.out.println("getTask(" + id + "," + build + ")");
        return new Task(id, "Hej", String.valueOf(build), StatusFactory.idle(), null, false, null);
    }

    public String getRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    @Override
    public Collection<TopLevelItem> getItems()
    {
        return Jenkins.getInstance().getItems();
    }

    @Override
    public boolean contains(TopLevelItem item)
    {
        return getItems().contains(item);
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return getOwner().getPrimaryView().doCreateItem(req, rsp);
    }


    @Extension
    public static class DescriptorImpl extends ViewDescriptor
    {
        public ListBoxModel doFillNoOfColumnsItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }
        public ListBoxModel doFillNoOfPipelinesItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            for(int i = 0; i <= 10; i++) {
                String opt = String.valueOf(i);
                options.add(opt, opt);
            }
            return options;
        }

        public ListBoxModel doFillSortingItems() {
            DescriptorExtensionList<ComponentComparator,ComponentComparatorDescriptor> descriptors =  ComponentComparator.all();
            ListBoxModel options = new ListBoxModel();
            for (ComponentComparatorDescriptor descriptor : descriptors) {
                options.add(descriptor.getDisplayName(), descriptor.getId());
            }
            return options;
        }

        @Override
        public String getDisplayName() {
            return "Delivery Pipeline View";
        }
    }


    public static class ComponentSpec extends AbstractDescribableImpl<ComponentSpec>
    {
        private String name;
        private String firstJob;

        @DataBoundConstructor
        public ComponentSpec(String name, String firstJob) {
            this.name = name;
            this.firstJob = firstJob;
        }

        public String getName() {
            return name;
        }

        public String getFirstJob() {
            return firstJob;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFirstJob(String firstJob) {
            this.firstJob = firstJob;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<ComponentSpec> {

            @Override
            public String getDisplayName() {
                return "";
            }

            public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
                return ProjectUtil.fillAllProjects(context);
            }

            public FormValidation doCheckName(@QueryParameter String value) {
                if (value != null && !value.trim().equals("")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Please supply a title!");
                }
            }

        }
    }
}
