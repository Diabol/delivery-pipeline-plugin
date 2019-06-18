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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.PredefinedBuildParameters;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.security.ACL;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import hudson.tasks.BuildTrigger;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import net.sf.json.JSONObject;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.WithoutJenkins;
import org.kohsuke.stapler.StaplerRequest;

import org.mockito.junit.MockitoJUnitRunner;
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.Pipeline;
import se.diabol.jenkins.pipeline.domain.Stage;
import se.diabol.jenkins.pipeline.domain.task.Task;
import se.diabol.jenkins.pipeline.sort.NameComparator;
import se.diabol.jenkins.pipeline.trigger.TriggerException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DeliveryPipelineViewTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    private static final String NONE = null;
    private static final boolean DO_NOT_SHOW_UPSTREAM = false;

    @Test
    public void testOnJobRenamed() throws Exception {
        final FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");

        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1", NONE, DO_NOT_SHOW_UPSTREAM));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2", NONE, DO_NOT_SHOW_UPSTREAM));

        DeliveryPipelineView view = new DeliveryPipelineView("Test");
        view.setComponentSpecs(componentSpecs);
        jenkins.getInstance().addView(view);

        p1.renameTo("newbuild");

        assertEquals("newbuild", view.getComponentSpecs().get(0).getFirstJob());
    }

    @Test
    public void testOnLastJobRenamed() throws Exception {
        final FreeStyleProject p2 = jenkins.createFreeStyleProject("build2");

        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1", "build2", DO_NOT_SHOW_UPSTREAM));

        DeliveryPipelineView view = new DeliveryPipelineView("Test");
        view.setComponentSpecs(componentSpecs);
        jenkins.getInstance().addView(view);

        p2.renameTo("newbuild");
        assertEquals("newbuild", view.getComponentSpecs().get(0).getLastJob());

        p2.delete();
        assertEquals(0, view.getComponentSpecs().size());
    }

    @Test
    @WithoutJenkins
    @Issue("JENKINS-23373")
    public void testOnJobRenamedNoComponentSpecs() {
        DeliveryPipelineView view = new DeliveryPipelineView("Test");
        //Rename
        view.onProjectRenamed(null, "build1", "newbuild");
        //Delete
        view.onProjectRenamed(null, "build1", null);
    }

    @Test
    public void testOnJobRenamedDelete() throws Exception {
        final FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");

        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1", NONE, DO_NOT_SHOW_UPSTREAM));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2", NONE, DO_NOT_SHOW_UPSTREAM));

        DeliveryPipelineView view = new DeliveryPipelineView("Test");
        view.setComponentSpecs(componentSpecs);

        jenkins.getInstance().addView(view);

        assertEquals(2, view.getComponentSpecs().size());

        p1.delete();

        assertEquals(1, view.getComponentSpecs().size());
    }

    @Test
    @WithoutJenkins
    public void shouldSubmitForm() throws Exception {
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getSubmittedForm()).thenReturn(new JSONObject());
        view.submit(request);
        verify(request, times(1)).bindJSON(view, new JSONObject());
        verify(request, times(1)).bindJSONToList(DeliveryPipelineView.ComponentSpec.class, null);
        verify(request, times(1)).bindJSONToList(DeliveryPipelineView.RegExpSpec.class, null);
    }

    @Test
    @WithoutJenkins
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void shouldValidateCheckUpdateInterval() {
        DeliveryPipelineView.DescriptorImpl descriptor = new DeliveryPipelineView.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckUpdateInterval("1").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckUpdateInterval("3").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval(null).kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval("0").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval("3a").kind);
    }

    @Test
    @WithoutJenkins
    public void shouldHaveDefaults() {
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        assertEquals(3, view.getNoOfPipelines());
        assertEquals(1, view.getNoOfColumns());
        assertEquals(2, view.getUpdateInterval());
        assertEquals("none", view.getSorting());
        assertNull(view.getEmbeddedCss());
        assertNull(view.getFullScreenCss());
        assertNull(view.getComponentSpecs());
        assertFalse(view.isShowAggregatedPipeline());
        assertFalse(view.getShowAvatars());
        assertFalse(view.isShowChanges());
        assertFalse(view.isAllowManualTriggers());
        assertFalse(view.isShowTotalBuildTime());
        assertFalse(view.isAllowRebuild());
        assertFalse(view.isShowDescription());
        assertFalse(view.isShowPromotions());
        assertFalse(view.isShowTestResults());
        assertFalse(view.isShowStaticAnalysisResults());
        assertFalse(view.isLinkRelative());
        assertFalse(view.getPagingEnabled());
        assertFalse(view.isAllowPipelineStart());
        assertFalse(view.isAllowAbort());
        assertEquals(-1, view.getMaxNumberOfVisiblePipelines());
        assertFalse(view.isShowAggregatedChanges());
        assertNull(view.getAggregatedChangesGroupingPattern());
        assertFalse(view.isLinkToConsoleLog());
    }

    @Test
    @WithoutJenkins
    public void testSettersAndGetters() {
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        view.setNoOfPipelines(17);
        assertEquals(17, view.getNoOfPipelines());
        view.setShowChanges(true);
        assertTrue(view.isShowChanges());
        view.setShowChanges(false);
        assertFalse(view.isShowChanges());
        view.setNoOfColumns(2);
        assertEquals(2, view.getNoOfColumns());
        view.setShowAvatars(true);
        assertTrue(view.getShowAvatars());
        view.setShowAvatars(false);
        assertFalse(view.getShowAvatars());
        assertNotNull(view.getLastUpdated());
        view.setAllowManualTriggers(true);
        assertTrue(view.isAllowManualTriggers());
        view.setShowTotalBuildTime(true);
        assertTrue(view.isShowTotalBuildTime());
        view.setAllowRebuild(true);
        assertTrue(view.isAllowRebuild());
        view.setShowDescription(true);
        assertTrue(view.isShowDescription());
        view.setShowPromotions(true);
        assertTrue(view.isShowPromotions());
        view.setShowTestResults(true);
        assertTrue(view.isShowTestResults());
        view.setShowStaticAnalysisResults(true);
        assertTrue(view.isShowStaticAnalysisResults());
        view.setLinkRelative(true);
        assertTrue(view.isLinkRelative());
        view.setPagingEnabled(true);
        assertTrue(view.getPagingEnabled());
        view.setAllowPipelineStart(true);
        assertTrue(view.isAllowPipelineStart());
        view.setMaxNumberOfVisiblePipelines(10);
        assertEquals(10, view.getMaxNumberOfVisiblePipelines());
        view.setShowAggregatedChanges(true);
        assertTrue(view.isShowAggregatedChanges());
        view.setAggregatedChangesGroupingPattern("TestRegex");
        assertEquals("TestRegex", view.getAggregatedChangesGroupingPattern());
        view.setLinkToConsoleLog(true);
        assertTrue(view.isLinkToConsoleLog());
    }

    @Test
    @WithoutJenkins
    public void testCssUrl() {
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        view.setEmbeddedCss("");
        view.setFullScreenCss("");
        assertNull(view.getEmbeddedCss());
        assertNull(view.getFullScreenCss());
    }

    @Test
    @WithoutJenkins
    public void testOldSorter() throws Exception {
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        Field field = view.getClass().getDeclaredField("sorting");
        field.setAccessible(true);
        field.set(view, "se.diabol.jenkins.pipeline.sort.NoOpComparator");
        assertEquals("none", view.getSorting());

        view.setSorting("se.diabol.jenkins.pipeline.sort.NoOpComparator");
        assertEquals("none", view.getSorting());
    }

    @Test
    @WithoutJenkins
    public void testSetSorting() {
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        view.setSorting("se.diabol.jenkins.pipeline.sort.NameComparator");
        assertEquals("se.diabol.jenkins.pipeline.sort.NameComparator", view.getSorting());
    }

    @Test
    public void testGetItemsAndContains() throws Exception {
        final FreeStyleProject build = jenkins.createFreeStyleProject("build");
        final FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");
        final FreeStyleProject packaging = jenkins.createFreeStyleProject("packaging");
        build.getPublishersList().add(new BuildTrigger("sonar", false));
        build.getPublishersList().add(new BuildTrigger("packaging", false));

        jenkins.getInstance().rebuildDependencyGraph();

        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build", NONE, DO_NOT_SHOW_UPSTREAM));
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        view.setComponentSpecs(specs);
        jenkins.getInstance().addView(view);

        assertTrue(view.contains(build));
        assertTrue(view.contains(sonar));
        assertTrue(view.contains(packaging));

        Collection<TopLevelItem> items =  view.getItems();
        assertEquals(3, items.size());
    }

    @Test
    public void testGetItemsGetPipelinesWhenNoProjectFound() throws Exception {
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build", NONE, DO_NOT_SHOW_UPSTREAM));
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        view.setComponentSpecs(specs);
        jenkins.getInstance().addView(view);
        Collection<TopLevelItem> items = view.getItems();
        assertNotNull(items);
        assertEquals(0, items.size());

        List<Component> components = view.getPipelines();
        assertNotNull(components);
        assertTrue(components.isEmpty());
        assertNotNull(view.getError());
    }

    @Test
    public void testGetItemsAndContainsWithFolders() throws Exception {
        MockFolder folder = jenkins.createFolder("folder");
        final FreeStyleProject build = folder.createProject(FreeStyleProject.class, "build");
        final FreeStyleProject sonar = folder.createProject(FreeStyleProject.class, "sonar");
        final FreeStyleProject packaging = folder.createProject(FreeStyleProject.class, "packaging");

        build.getPublishersList().add(new BuildTrigger("sonar", false));
        build.getPublishersList().add(new BuildTrigger("packaging", false));

        jenkins.getInstance().rebuildDependencyGraph();

        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build", NONE, DO_NOT_SHOW_UPSTREAM));
        DeliveryPipelineView view = new DeliveryPipelineView("name");
        view.setComponentSpecs(specs);
        folder.addView(view);

        assertTrue(view.contains(build));
        assertTrue(view.contains(sonar));
        assertTrue(view.contains(packaging));

        Collection<TopLevelItem> items = view.getItems();
        assertEquals(3, items.size());
    }

    @Test
    public void testGetPipelineViewWithLastJobProvided() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject test = jenkins.createFreeStyleProject("test");
        FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");

        build.getPublishersList().add(new BuildTrigger(test.getName(), false));
        test.getPublishersList().add(new BuildTrigger(deploy.getName(), false));

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();

        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build", "test", DO_NOT_SHOW_UPSTREAM));
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setComponentSpecs(specs);
        view.setSorting(NameComparator.class.getName());
        jenkins.getInstance().addView(view);
        view.getPipelines();
        assertNull(view.getError());
    }

    @Test
    public void testGetPipelinesUsesMaxNumberOfJobs() throws Exception {
        jenkins.createFreeStyleProject("build");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build", NONE, DO_NOT_SHOW_UPSTREAM));
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp1", "build", NONE, DO_NOT_SHOW_UPSTREAM));
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setComponentSpecs(specs);
        view.setMaxNumberOfVisiblePipelines(1);
        jenkins.getInstance().addView(view);
        List<Component> pipelines = view.getPipelines();
        assertEquals(1, pipelines.size());
        assertNull(view.getError());
    }

    @Test
    public void allJobsAreReturnedWhenMaxNotSet() throws Exception {
        jenkins.createFreeStyleProject("build");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            specs.add(new DeliveryPipelineView.ComponentSpec("Comp" + i, "build", NONE, DO_NOT_SHOW_UPSTREAM));
        }
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setComponentSpecs(specs);
        jenkins.getInstance().addView(view);
        List<Component> pipelines = view.getPipelines();
        assertEquals(specs.size(), pipelines.size());
        assertNull(view.getError());
    }

    @Test
    public void testMaxItemsWorksWithRegexp() throws Exception {
        jenkins.createFreeStyleProject("compile-Project1");
        jenkins.createFreeStyleProject("compile-Project2");
        jenkins.createFreeStyleProject("compile-Project3");

        boolean showUpstream = false;
        DeliveryPipelineView.RegExpSpec regExpSpec = new DeliveryPipelineView.RegExpSpec("^compile-(.*)", showUpstream);
        assertFalse(showUpstream);
        List<DeliveryPipelineView.RegExpSpec> regExpSpecs = new ArrayList<>();
        regExpSpecs.add(regExpSpec);

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setRegexpFirstJobs(regExpSpecs);
        view.setMaxNumberOfVisiblePipelines(2);
        assertEquals(regExpSpecs, view.getRegexpFirstJobs());

        jenkins.getInstance().addView(view);

        List<Component> components = view.getPipelines();
        assertNull(view.getError());
        assertEquals(2, components.size());
    }

    @Test
    public void testGetPipelines() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        build.addProperty(new PipelineProperty("Build", "BuildStage", ""));
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Comp", "build", NONE, DO_NOT_SHOW_UPSTREAM));
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setComponentSpecs(specs);
        view.setSorting(NameComparator.class.getName());
        jenkins.getInstance().addView(view);
        List<Component> components = view.getPipelines();
        assertNull(view.getError());
        assertEquals(1, components.size());
        Component component = components.get(0);
        assertEquals(0, component.getPipelines().size());
        assertEquals("Comp", component.getName());

        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(build);

        view.setShowChanges(false);
        components = view.getPipelines();
        Pipeline pipeline = components.get(0).getPipelines().get(0);
        assertNull(pipeline.getContributors());
        assertNull(pipeline.getChanges());

        view.setShowChanges(true);
        components = view.getPipelines();
        assertNull(view.getError());
        assertEquals(1, components.size());
        component = components.get(0);
        assertEquals(1, component.getPipelines().size());
        assertEquals("Comp", component.getName());
        pipeline = component.getPipelines().get(0);
        assertEquals("#1", pipeline.getVersion());
        assertNotNull(pipeline.getTimestamp());
        assertFalse(pipeline.isAggregated());
        assertEquals(1, pipeline.getTriggeredBy().size());
        assertEquals(0, pipeline.getContributors().size());
        assertEquals(1, pipeline.getStages().size());
        assertEquals(0, pipeline.getChanges().size());

        Stage stage = pipeline.getStages().get(0);
        assertEquals("BuildStage", stage.getName());
        assertEquals(1, stage.getTasks().size());
        Task task = stage.getTasks().get(0);
        assertEquals("Build", task.getName());
        assertEquals("build", task.getId());
        assertEquals("1", task.getBuildId());
        assertEquals(0, task.getTestResults().size());

        view.setShowAggregatedPipeline(true);
        components = view.getPipelines();
        assertNull(view.getError());
        assertEquals(1, components.size());
        component = components.get(0);
        assertEquals(2, component.getPipelines().size());
        assertEquals("Comp", component.getName());

        pipeline = component.getPipelines().get(0);
        assertNull(pipeline.getVersion());
        assertNull(pipeline.getTimestamp());
        assertTrue(pipeline.isAggregated());
        assertNull(pipeline.getTriggeredBy());
        assertNull(pipeline.getContributors());
        assertEquals(1, pipeline.getStages().size());
        assertNull(pipeline.getChanges());

        pipeline = component.getPipelines().get(1);
        assertEquals("#1", pipeline.getVersion());
        assertNotNull(pipeline.getTimestamp());
        assertFalse(pipeline.isAggregated());
        assertEquals(1, pipeline.getTriggeredBy().size());
        assertEquals(0, pipeline.getContributors().size());
        assertEquals(1, pipeline.getStages().size());
        assertEquals(0, pipeline.getChanges().size());
    }

    @Test
    @WithoutJenkins
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void testDoCheckName() {
        DeliveryPipelineView.ComponentSpec.DescriptorImpl descriptor =
                new DeliveryPipelineView.ComponentSpec.DescriptorImpl();
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckName(null).kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckName("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckName(" ").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckName("Component").kind);
    }

    @Test
    @WithoutJenkins
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void testDoCheckRegexpFirstJob() {
        DeliveryPipelineView.RegExpSpec.DescriptorImpl descriptor =
                new DeliveryPipelineView.RegExpSpec.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckRegexp(null).kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRegexp(" ").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRegexp(" \t\r\n ").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRegexp("*").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRegexp("^build-.+?-project").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckRegexp("^build-(.+?)-project").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRegexp("^build-(.+?)-(project)").kind);
    }

    @Test
    @WithoutJenkins
    public void testUpdateInterval() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setUpdateInterval(0);
        assertEquals(2, view.getUpdateInterval());
    }

    @Test
    @WithoutJenkins
    public void testFullScreenCss() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setFullScreenCss(null);
        assertNull(view.getFullScreenCss());
        view.setFullScreenCss(" ");
        assertNull(view.getFullScreenCss());
        view.setFullScreenCss("http://somewhere.com");
        assertEquals("http://somewhere.com", view.getFullScreenCss());
    }

    @Test
    @WithoutJenkins
    public void testEmbeddedCss() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setEmbeddedCss(null);
        assertNull(view.getEmbeddedCss());
        view.setEmbeddedCss(" ");
        assertNull(view.getEmbeddedCss());
        view.setEmbeddedCss("http://somewhere.com");
        assertEquals("http://somewhere.com", view.getEmbeddedCss());
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

    @Test
    public void testDoFillFirstJobItems() {
        ListBoxModel model =
                new DeliveryPipelineView.ComponentSpec.DescriptorImpl().doFillFirstJobItems(jenkins.getInstance());
        assertNotNull(model);
    }

    @Test
    public void testDoFillLastJobItems() {
        ListBoxModel model =
                new DeliveryPipelineView.ComponentSpec.DescriptorImpl().doFillLastJobItems(jenkins.getInstance());
        assertNotNull(model);
    }

    @Test
    public void testGetPipelinesRegExp() throws Exception {
        jenkins.createFreeStyleProject("compile-Project1");
        jenkins.createFreeStyleProject("compile-Project2");
        jenkins.createFreeStyleProject("compile-Project3");
        jenkins.createFreeStyleProject("compile");

        final boolean showUpstream = true;
        final boolean doNotShowUpstream = false;
        DeliveryPipelineView.RegExpSpec regExpSpec = new DeliveryPipelineView.RegExpSpec("^compile-(.*)", doNotShowUpstream);
        assertThat(regExpSpec.isShowUpstream(), is(doNotShowUpstream));
        regExpSpec.setShowUpstream(showUpstream);
        assertThat(regExpSpec.isShowUpstream(), is(showUpstream));
        List<DeliveryPipelineView.RegExpSpec> regExpSpecs = new ArrayList<>();
        regExpSpecs.add(regExpSpec);

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setRegexpFirstJobs(regExpSpecs);
        assertEquals(regExpSpecs, view.getRegexpFirstJobs());

        jenkins.getInstance().addView(view);

        List<Component> components = view.getPipelines();
        assertNull(view.getError());
        assertEquals(3, components.size());

        List<String> names = new ArrayList<>();

        for (Component component : components) {
            names.add(component.getName());
        }

        assertTrue(names.contains("Project1"));
        assertTrue(names.contains("Project2"));
        assertTrue(names.contains("Project3"));

        assertEquals(3, view.getItems().size());
    }

    @Test
    @WithoutJenkins
    public void testGetApi() {
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        Api api = view.getApi();
        assertTrue(api instanceof PipelineApi);
    }

    @Test
    @WithoutJenkins
    public void withoutFolderPrefixShouldRemoveFolderPrefixIfPresentInProjectName() {
        final String projectName = "Job1";
        final String projectNameWithFolderPrefix = "Folder1/" + projectName;
        assertEquals(projectName, DeliveryPipelineView.withoutFolderPrefix(projectNameWithFolderPrefix));
    }

    @Test
    @WithoutJenkins
    public void withoutFolderPrefixShouldReturnProjectNameIfNoFolderPrefixIsPresent() {
        final String projectNameWithoutFolderPrefix = "Job2";
        assertEquals(projectNameWithoutFolderPrefix,
                DeliveryPipelineView.withoutFolderPrefix(projectNameWithoutFolderPrefix));
    }

    @Test
    @Issue("JENKINS-23532")
    @WithoutJenkins
    public void triggerExceptionMessageShouldSuggestRemovingFolderPrefixIfPresent() {
        final String projectName = "Job3";
        final String projectNameWithFolderPrefix = "Folder2/" + projectName;
        final String exceptionMessage =
                DeliveryPipelineView.triggerExceptionMessage(projectNameWithFolderPrefix, "upstream", "1");
        assertTrue(exceptionMessage.contains(projectNameWithFolderPrefix));
        assertTrue(exceptionMessage.contains("Did you mean to specify " + projectName + "?"));
    }

    @Test
    @Ignore("Core UI upgrade broke this testcase")
    public void testDoCreateItem() throws Exception {
        testDoCreateItem("testDoCreateItem", "");

        DeliveryPipelineView view = new DeliveryPipelineView("Delivery Pipeline");
        jenkins.getInstance().addView(view);

        testDoCreateItem("testDoCreateItemAsTheDefaultViewFromTheViewUrl", "view/Delivery%20Pipeline/");

        jenkins.getInstance().setPrimaryView(view);
        testDoCreateItem("testDoCreateItemAsTheDefaultView", "");
    }

    private void testDoCreateItem(String projectName, String baseUrl) throws Exception {
        HtmlPage page = jenkins.createWebClient().goTo(baseUrl + "newJob");
        HtmlForm form = page.getFormByName("createItem");
        form.getInputByName("name").setValueAttribute(projectName);
        form.getInputByName("mode").setValueAttribute("hudson.model.FreeStyleProject");
        HtmlPage result = jenkins.submit(form);

        assertTrue(jenkins.jenkins.getJobNames().contains(projectName));
    }

    @Test
    public void testTriggerManualNoTriggerFound() throws Exception {
        jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");
        DeliveryPipelineView view = new DeliveryPipelineView("View");
        try {
            view.triggerManual("B", "A", "#1");
            fail();
        } catch (TriggerException e) {
            //Should throw this
        } catch (AuthenticationException e) {
            fail();
        }
    }

    @Test
    public void testTriggerManualNoBuildFound() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));

        jenkins.getInstance().rebuildDependencyGraph();
        DeliveryPipelineView view = new DeliveryPipelineView("View");
        jenkins.getInstance().addView(view);
        try {
            view.triggerManual("B", "A", "#1");
            fail();
        } catch (TriggerException e) {
            //Should throw this
        } catch (AuthenticationException e) {
            fail();
        }
    }

    @Test
    public void testTriggerManualNotAuthorized() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));

        jenkins.getInstance().rebuildDependencyGraph();
        DeliveryPipelineView view = new DeliveryPipelineView("View");
        jenkins.getInstance().addView(view);

        jenkins.getInstance().setSecurityRealm(jenkins.createDummySecurityRealm());
        GlobalMatrixAuthorizationStrategy gmas = new GlobalMatrixAuthorizationStrategy();
        gmas.add(Permission.READ, "devel");
        jenkins.getInstance().setAuthorizationStrategy(gmas);

        SecurityContext oldContext = ACL.impersonate(User.get("devel").impersonate());
        try {
            view.triggerManual("B", "A", "#1");
            fail();
        } catch (TriggerException e) {
            fail();
        } catch (AuthenticationException e) {
            //Should throw this
        }
        SecurityContextHolder.setContext(oldContext);
    }

    @Test
    @Issue("JENKINS-22658")
    public void testRecursiveStages() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        projectA.addProperty(new PipelineProperty("A", "A", ""));
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectB.addProperty(new PipelineProperty("B", "B", ""));
        FreeStyleProject projectC = jenkins.createFreeStyleProject("C");
        projectC.addProperty(new PipelineProperty("C", "C", ""));
        FreeStyleProject projectD = jenkins.createFreeStyleProject("D");
        projectD.addProperty(new PipelineProperty("D", "B", ""));

        projectA.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("B", ResultCondition.SUCCESS, new ArrayList<>())));
        projectB.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("C", ResultCondition.SUCCESS, new ArrayList<>())));
        projectC.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("D", ResultCondition.SUCCESS, new ArrayList<>())));

        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("Comp", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(componentSpecs);

        jenkins.getInstance().addView(view);

        List<Component> components = view.getPipelines();
        assertEquals(0, components.size());
        assertNotNull(view.getError());
        assertTrue(view.getError().startsWith("Circular dependencies between stages: "));
        assertTrue(view.getError().contains("B"));
        assertTrue(view.getError().contains("C"));
    }

    @Test
    public void testInvalidSorter() throws Exception {
        jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("Comp2", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("Comp1", "B", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(componentSpecs);
        view.setShowAggregatedPipeline(true);
        view.setSorting("this will not be found");
        jenkins.getInstance().addView(view);

        List<Component> components = view.getPipelines();
        assertEquals(2, components.size());
        assertEquals("Comp2", components.get(0).getName());
        assertEquals("Comp1", components.get(1).getName());

        view.setSorting(null);

        components = view.getPipelines();
        assertEquals(2, components.size());
        assertEquals("Comp2", components.get(0).getName());
        assertEquals("Comp1", components.get(1).getName());
    }

    @Test
    public void testNoneSorter() throws Exception {
        jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("Comp2", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("Comp1", "B", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(componentSpecs);
        view.setShowAggregatedPipeline(true);
        view.setSorting("none");
        jenkins.getInstance().addView(view);

        List<Component> components = view.getPipelines();
        assertEquals(2, components.size());
        assertEquals("Comp2", components.get(0).getName());
        assertEquals("Comp1", components.get(1).getName());
        assertEquals("A", components.get(0).getFirstJob());
        assertEquals("B", components.get(1).getFirstJob());
    }

    @Test
    public void testRebuild() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectB.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("BUILD_VERSION", "DEFAULT_VALUE")));
        projectA.getPublishersList().add(
                new hudson.plugins.parameterizedtrigger.BuildTrigger(
                        new BuildTriggerConfig("b", ResultCondition.SUCCESS,
                                new PredefinedBuildParameters("VERSION=$BUILD_NUMBER"))));

        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        jenkins.getInstance().addView(view);

        jenkins.buildAndAssertSuccess(projectA);
        jenkins.waitUntilNoActivity();

        assertNotNull(projectA.getLastBuild());
        assertNotNull(projectB.getLastBuild());

        final AbstractBuild<?, ?> b1 = projectB.getLastBuild();

        view.triggerRebuild("B", "1");
        jenkins.waitUntilNoActivity();
        assertEquals(2, projectB.getLastBuild().getNumber());
        assertEqualsList(b1.getActions(ParametersAction.class),
                projectB.getLastBuild().getActions(ParametersAction.class));
    }

    @Test
    public void testRebuildNotAuthorized() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));

        jenkins.getInstance().rebuildDependencyGraph();
        DeliveryPipelineView view = new DeliveryPipelineView("View");
        jenkins.getInstance().addView(view);

        jenkins.getInstance().setSecurityRealm(jenkins.createDummySecurityRealm());
        GlobalMatrixAuthorizationStrategy gmas = new GlobalMatrixAuthorizationStrategy();
        gmas.add(Permission.READ, "devel");
        jenkins.getInstance().setAuthorizationStrategy(gmas);

        SecurityContext oldContext = ACL.impersonate(User.get("devel").impersonate());
        try {
            view.triggerRebuild("B", "1");
            fail();
        } catch (AuthenticationException e) {
            //Should throw this
        }
        SecurityContextHolder.setContext(oldContext);
    }

    @Test
    public void testComponentSpecDescriptorImpldoFillFirstJobItems() throws Exception {
        jenkins.createFreeStyleProject("a");
        assertEquals(1, new DeliveryPipelineView.ComponentSpec.DescriptorImpl().doFillFirstJobItems(
                jenkins.getInstance()).size());
    }

    @Test
    public void testGetItems() throws IOException {
        final FreeStyleProject firstJob = jenkins.createFreeStyleProject("Project1");
        final FreeStyleProject secondJob = jenkins.createFreeStyleProject("Project2");
        final FreeStyleProject thirdJob = jenkins.createFreeStyleProject("Project3");

        firstJob.getPublishersList().add((new BuildTrigger(secondJob.getName(), true)));
        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView pipeline = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("Spec", firstJob.getName(), NONE, DO_NOT_SHOW_UPSTREAM));
        pipeline.setComponentSpecs(componentSpecs);
        jenkins.getInstance().addView(pipeline);

        Collection<TopLevelItem> jobs = pipeline.getItems();
        assertTrue(jobs.contains(firstJob));
        assertTrue(jobs.contains(secondJob));
        assertFalse(jobs.contains(thirdJob));
    }

    @Test
    @WithoutJenkins
    public void shouldNotShowPagingForFullScreenViewWhenPagingEnabled() {
        DeliveryPipelineView view = mock(DeliveryPipelineView.class);
        when(view.isFullScreenView()).thenReturn(true);
        when(view.showPaging()).thenCallRealMethod();
        assertFalse(view.showPaging());
    }

    @Test
    @WithoutJenkins
    public void shouldNotShowPagingForFullScreenViewWhenPagingDisabled() {
        DeliveryPipelineView view = mock(DeliveryPipelineView.class);
        when(view.isFullScreenView()).thenReturn(true);
        when(view.showPaging()).thenCallRealMethod();
        assertFalse(view.showPaging());
    }

    @Test
    @WithoutJenkins
    public void shouldShowPagingForNormalViewWhenPagingEnabled() {
        DeliveryPipelineView view = mock(DeliveryPipelineView.class);
        when(view.isFullScreenView()).thenReturn(false);
        when(view.getPagingEnabled()).thenReturn(true);
        when(view.showPaging()).thenCallRealMethod();
        assertTrue(view.showPaging());
    }

    @Test
    @WithoutJenkins
    public void shouldNotShowPagingForNormalViewWhenPagingDisabled() {
        DeliveryPipelineView view = mock(DeliveryPipelineView.class);
        when(view.isFullScreenView()).thenReturn(false);
        when(view.getPagingEnabled()).thenReturn(false);
        when(view.showPaging()).thenCallRealMethod();
        assertFalse(view.showPaging());
    }

    @Test
    @WithoutJenkins
    public void getDescriptionShouldSetSuperDescriptionIfNotSet() {
        DeliveryPipelineView view = mock(DeliveryPipelineView.class);
        doCallRealMethod().when(view).getDescription();
        doCallRealMethod().when(view).setDescription(anyString());

        String description = view.getDescription();
        verify(view, times(1)).setDescription(any());
        assertNull(description);

        String expectedDescription = "some description";
        view.setDescription(expectedDescription);
        assertNotNull(view.getDescription());
        assertThat(view.getDescription(), is(expectedDescription));
        verify(view, times(2)).setDescription(any());

        view.getDescription();
        verify(view, times(2)).setDescription(any());
    }

    private void assertEqualsList(List<ParametersAction> a1, List<ParametersAction> a2) {
        if (a1.size() != a2.size()) {
            throw new ComparisonFailure("Size not equal!", String.valueOf(a1.size()), String.valueOf(a2.size()));
        }
        for (int i = 0; i < a1.size(); i++) {
            ParametersAction action1 = a1.get(i);
            ParametersAction action2 = a2.get(i);
            assertEquals(action1.getParameters(), action2.getParameters());
        }
    }
}
