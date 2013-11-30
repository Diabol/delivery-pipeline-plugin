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

import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItem;
import hudson.tasks.BuildTrigger;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.pipeline.model.Component;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;
import se.diabol.jenkins.pipeline.sort.NameComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class DeliveryPipelineViewTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testOnJobRenamed() {
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1"));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2"));

        DeliveryPipelineView view = new DeliveryPipelineView("Test", componentSpecs);
        view.onJobRenamed(null, "build1", "newbuild");
        assertEquals("newbuild", view.getComponentSpecs().get(0).getFirstJob());
    }

    @Test
    @WithoutJenkins
    public void testOnJobRenamedDelete() {
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1"));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2"));


        DeliveryPipelineView view = new DeliveryPipelineView("Test", componentSpecs);
        assertEquals(2, view.getComponentSpecs().size());
        view.onJobRenamed(null, "build1", null);

        assertEquals(1, view.getComponentSpecs().size());

    }

    @Test
    @WithoutJenkins
    public void testDoCheckUpdateInterval() {
        DeliveryPipelineView.DescriptorImpl d = new DeliveryPipelineView.DescriptorImpl();
        assertEquals(FormValidation.Kind.ERROR, d.doCheckUpdateInterval("").kind);
        assertEquals(FormValidation.Kind.ERROR, d.doCheckUpdateInterval(null).kind);
        assertEquals(FormValidation.Kind.OK, d.doCheckUpdateInterval("3").kind);
        assertEquals(FormValidation.Kind.ERROR, d.doCheckUpdateInterval("3a").kind);
        assertEquals(FormValidation.Kind.ERROR, d.doCheckUpdateInterval("0").kind);
        assertEquals(FormValidation.Kind.OK, d.doCheckUpdateInterval("1").kind);
    }

    @Test
    @WithoutJenkins
    public void testDefaults() {
        DeliveryPipelineView view = new DeliveryPipelineView("name", new ArrayList<DeliveryPipelineView.ComponentSpec>());
        assertEquals(3, view.getNoOfPipelines());
        assertEquals(1, view.getNoOfColumns());
        assertEquals(2, view.getUpdateInterval());
        assertEquals("none", view.getSorting());
        assertNull(view.getEmbeddedCss());
        assertNull(view.getFullScreenCss());
        assertFalse(view.isShowAggregatedPipeline());
        assertFalse(view.getShowAvatars());
        assertFalse(view.isShowChanges());
    }

    @Test
    public void testGetItemsAndContains() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");
        FreeStyleProject packaging = jenkins.createFreeStyleProject("packaging");
        build.getPublishersList().add(new BuildTrigger("sonar", false));
        build.getPublishersList().add(new BuildTrigger("packaging", false));

        jenkins.getInstance().rebuildDependencyGraph();


        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build"));
        DeliveryPipelineView view = new DeliveryPipelineView("name", specs);
        jenkins.getInstance().addView(view);

        assertTrue(view.contains(build));
        assertTrue(view.contains(sonar));
        assertTrue(view.contains(packaging));

        Collection<TopLevelItem> items =  view.getItems();
        assertEquals(3, items.size());

    }

    @Test
    public void testGetItemsAndContainsWithFolders() throws Exception {
        MockFolder folder = jenkins.createFolder("folder");
        FreeStyleProject build = folder.createProject(FreeStyleProject.class, "build");
        FreeStyleProject sonar = folder.createProject(FreeStyleProject.class, "sonar");
        FreeStyleProject packaging = folder.createProject(FreeStyleProject.class,"packaging");


        //FreeStyleProject build = jenkins.createFreeStyleProject("build");
        //FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");
        build.getPublishersList().add(new BuildTrigger("sonar", false));
        build.getPublishersList().add(new BuildTrigger("packaging", false));

        jenkins.getInstance().rebuildDependencyGraph();


        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build"));
        DeliveryPipelineView view = new DeliveryPipelineView("name", specs);
        folder.addView(view);

        assertTrue(view.contains(build));
        assertTrue(view.contains(sonar));
        assertTrue(view.contains(packaging));

        Collection<TopLevelItem> items =  view.getItems();
        assertEquals(3, items.size());

    }


    @Test
    public void testGetPipelines() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        build.addProperty(new PipelineProperty("Build", "BuildStage"));
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build"));
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", specs);
        view.setSorting(NameComparator.class.getName());
        jenkins.getInstance().addView(view);
        List<Component> components = view.getPipelines();
        assertEquals(1, components.size());
        Component component = components.get(0);
        assertEquals(0, component.getPipelines().size());
        assertEquals("Comp", component.getName());

        jenkins.buildAndAssertSuccess(build);

        components = view.getPipelines();
        assertEquals(1, components.size());
        component = components.get(0);
        assertEquals(1, component.getPipelines().size());
        assertEquals("Comp", component.getName());
        Pipeline pipeline = component.getPipelines().get(0);
        assertEquals("#1", pipeline.getVersion());
        assertNotNull(pipeline.getTimestamp());
        assertFalse(pipeline.isAggregated());
        assertEquals(0, pipeline.getTriggeredBy().size());
        assertEquals(1, pipeline.getStages().size());
        assertEquals(0, pipeline.getChanges().size());

        Stage stage = pipeline.getStages().get(0);
        assertEquals("BuildStage", stage.getName());
        assertEquals(1, stage.getTasks().size());
        Task task = stage.getTasks().get(0);
        assertEquals("Build", task.getName());
        assertEquals("build", task.getId());
        assertEquals("1", task.getBuildId());
        assertNull(task.getTestResult());


        view.setShowAggregatedPipeline(true);
        components = view.getPipelines();
        assertEquals(1, components.size());
        component = components.get(0);
        assertEquals(2, component.getPipelines().size());
        assertEquals("Comp", component.getName());



        pipeline = component.getPipelines().get(0);
        assertNull(pipeline.getVersion());
        assertNull(pipeline.getTimestamp());
        assertTrue(pipeline.isAggregated());
        assertNull(pipeline.getTriggeredBy());
        assertEquals(1, pipeline.getStages().size());
        assertNull(pipeline.getChanges());



        pipeline = component.getPipelines().get(1);
        assertEquals("#1", pipeline.getVersion());
        assertNotNull(pipeline.getTimestamp());
        assertFalse(pipeline.isAggregated());
        assertEquals(0, pipeline.getTriggeredBy().size());
        assertEquals(1, pipeline.getStages().size());
        assertEquals(0, pipeline.getChanges().size());



    }

    @Test
    @WithoutJenkins
    public void testDoCheckName() {
        DeliveryPipelineView.ComponentSpec.DescriptorImpl d = new DeliveryPipelineView.ComponentSpec.DescriptorImpl();
        assertEquals(FormValidation.Kind.ERROR,  d.doCheckName(null).kind);
        assertEquals(FormValidation.Kind.ERROR,  d.doCheckName("").kind);
        assertEquals(FormValidation.Kind.OK,  d.doCheckName("Component").kind);


    }

    @Test
    @WithoutJenkins
    public void testUpdateInterval() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", new ArrayList<DeliveryPipelineView.ComponentSpec>());
        view.setUpdateInterval(0);
        assertEquals(2, view.getUpdateInterval());
    }

    @Test
    @WithoutJenkins
    public void testFullScreenCss() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", new ArrayList<DeliveryPipelineView.ComponentSpec>());
        view.setFullScreenCss(null);
        assertNull(view.getFullScreenCss());
        view.setFullScreenCss(" ");
        assertNull(view.getFullScreenCss());

    }

    @Test
    @WithoutJenkins
    public void testEmbeddedCss() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", new ArrayList<DeliveryPipelineView.ComponentSpec>());
        view.setEmbeddedCss(null);
        assertNull(view.getEmbeddedCss());
        view.setEmbeddedCss(" ");
        assertNull(view.getEmbeddedCss());

    }

    @Test
    public void testDoFillSortingItems() {
        ListBoxModel model = new DeliveryPipelineView.DescriptorImpl().doFillSortingItems();
        assertNotNull(model);
        assertTrue(model.size() >= 1);
    }

    @Test
    public void testDoFillNoOfPipelinesItems() {
        ListBoxModel model = new DeliveryPipelineView.DescriptorImpl().doFillNoOfPipelinesItems(jenkins.getInstance());
        assertNotNull(model);
        assertTrue(model.size() != 0);
    }

    @Test
    public void testDoFillNoOfColumnsItems() {
        ListBoxModel model = new DeliveryPipelineView.DescriptorImpl().doFillNoOfColumnsItems(jenkins.getInstance());
        assertNotNull(model);
        assertTrue(model.size() != 0);
    }

}
