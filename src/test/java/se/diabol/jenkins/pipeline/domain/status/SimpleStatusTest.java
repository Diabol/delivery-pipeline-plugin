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
package se.diabol.jenkins.pipeline.domain.status;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.util.OneShotEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.MockBuilder;
import org.jvnet.hudson.test.UnstableBuilder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.diabol.jenkins.pipeline.domain.Pipeline;
import se.diabol.jenkins.pipeline.domain.status.promotion.AbstractPromotionStatusProvider;
import se.diabol.jenkins.pipeline.domain.status.promotion.PromotionStatus;
import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

@RunWith(MockitoJUnitRunner.class)
public class SimpleStatusTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private SimpleStatus.PromotionStatusProviderWrapper defaultNotMockedPromotionStatusProviderWrapper = new SimpleStatus.PromotionStatusProviderWrapper();

    @After
    public void tearDown() {
        SimpleStatus.setPromotionStatusProviderWrapper(defaultNotMockedPromotionStatusProviderWrapper);
    }

    @Test(expected=IllegalStateException.class)
    public void testResolveStatusNotRecognized() throws Exception {
        final AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(null);
        FreeStyleProject project = jenkins.createFreeStyleProject();

        SimpleStatus.resolveStatus(project, build, null);
        fail("Should throw exception");
    }

    @Test
    public void testResolveStatusIdle() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        Status status = SimpleStatus.resolveStatus(project, null, null);
        assertTrue(status.isIdle());
        assertEquals("IDLE", status.toString());
        assertEquals(-1, status.getLastActivity());
        assertEquals(-1, status.getDuration());
        assertNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.IDLE));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusDisabled() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.makeDisabled(true);
        Status status = SimpleStatus.resolveStatus(project, null, null);
        assertTrue(status.isDisabled());
        assertEquals("DISABLED", status.toString());
        assertEquals(-1, status.getLastActivity());
        assertEquals(-1, status.getDuration());
        assertNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.DISABLED));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        jenkins.buildAndAssertSuccess(project);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild(), null);
        assertTrue(status.isSuccess());
        assertEquals("SUCCESS", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.SUCCESS));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusSuccessWithPromotions() {
        final AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.SUCCESS);

        final List<PromotionStatus> promotionStatusList = new ArrayList<PromotionStatus>();
        promotionStatusList.add(Mockito.mock(PromotionStatus.class));
        promotionStatusList.add(Mockito.mock(PromotionStatus.class));
        promotionStatusList.add(Mockito.mock(PromotionStatus.class));

        final AbstractPromotionStatusProvider promotionStatusProvider = Mockito.mock(AbstractPromotionStatusProvider.class);
        Mockito.when(promotionStatusProvider.isBuildPromoted(build)).thenReturn(true);
        Mockito.when(promotionStatusProvider.getPromotionStatusList(build)).thenReturn(promotionStatusList);

        final List<AbstractPromotionStatusProvider> promotionStatusProviders = new ArrayList<AbstractPromotionStatusProvider>();
        promotionStatusProviders.add(promotionStatusProvider);

        final SimpleStatus.PromotionStatusProviderWrapper promotionStatusProviderWrapper = Mockito.mock(SimpleStatus.PromotionStatusProviderWrapper.class);
        Mockito.when(promotionStatusProviderWrapper.getAllPromotionStatusProviders()).thenReturn(promotionStatusProviders);

        SimpleStatus.setPromotionStatusProviderWrapper(promotionStatusProviderWrapper);

        final Status resolvedStatus = SimpleStatus.resolveStatus(null, build, null);
        assertTrue(resolvedStatus.isSuccess());
        assertEquals("SUCCESS", resolvedStatus.toString());
        assertTrue(resolvedStatus.getType().equals(StatusType.SUCCESS));
        assertEquals(3, resolvedStatus.getPromotions().size());
        assertTrue(resolvedStatus.isPromoted());
    }

    @Test
    public void testResolveStatusSuccessWithNoPromotionsPlugin() {
        final AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.SUCCESS);

        final SimpleStatus.PromotionStatusProviderWrapper promotionStatusProviderWrapper = Mockito.mock(SimpleStatus.PromotionStatusProviderWrapper.class);
        Mockito.when(promotionStatusProviderWrapper.getAllPromotionStatusProviders()).thenReturn(null);

        SimpleStatus.setPromotionStatusProviderWrapper(promotionStatusProviderWrapper);

        final Status resolvedStatus = SimpleStatus.resolveStatus(null, build, null);
        assertTrue(resolvedStatus.isSuccess());
        assertEquals("SUCCESS", resolvedStatus.toString());
        assertTrue(resolvedStatus.getType().equals(StatusType.SUCCESS));
        assertTrue(resolvedStatus.getPromotions().isEmpty());
        assertFalse(resolvedStatus.isPromoted());
    }

    @Test
    public void testResolveStatusFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new FailureBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild(), null);
        assertTrue(status.isFailed());
        assertEquals("FAILED", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.FAILED));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusFailureWithPromotions() {
        final AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.FAILURE);

        final List<PromotionStatus> promotionStatusList = new ArrayList<PromotionStatus>();
        promotionStatusList.add(Mockito.mock(PromotionStatus.class));
        promotionStatusList.add(Mockito.mock(PromotionStatus.class));
        promotionStatusList.add(Mockito.mock(PromotionStatus.class));

        final AbstractPromotionStatusProvider promotionStatusProvider = Mockito.mock(AbstractPromotionStatusProvider.class);
        Mockito.when(promotionStatusProvider.isBuildPromoted(build)).thenReturn(true);
        Mockito.when(promotionStatusProvider.getPromotionStatusList(build)).thenReturn(promotionStatusList);

        final List<AbstractPromotionStatusProvider> promotionStatusProviders = new ArrayList<AbstractPromotionStatusProvider>();
        promotionStatusProviders.add(promotionStatusProvider);

        final SimpleStatus.PromotionStatusProviderWrapper promotionStatusProviderWrapper = Mockito.mock(SimpleStatus.PromotionStatusProviderWrapper.class);
        Mockito.when(promotionStatusProviderWrapper.getAllPromotionStatusProviders()).thenReturn(promotionStatusProviders);

        SimpleStatus.setPromotionStatusProviderWrapper(promotionStatusProviderWrapper);

        final Status resolvedStatus = SimpleStatus.resolveStatus(null, build, null);
        assertFalse(resolvedStatus.isSuccess());
        assertEquals("FAILED", resolvedStatus.toString());
        assertTrue(resolvedStatus.getType().equals(StatusType.FAILED));
        assertEquals(3, resolvedStatus.getPromotions().size());
        assertTrue(resolvedStatus.isPromoted());
    }

    @Test
    public void testResolveStatusFailureWithNoPromotionsPlugin() {
        final AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.FAILURE);

        final SimpleStatus.PromotionStatusProviderWrapper promotionStatusProviderWrapper = Mockito.mock(SimpleStatus.PromotionStatusProviderWrapper.class);
        Mockito.when(promotionStatusProviderWrapper.getAllPromotionStatusProviders()).thenReturn(null);

        SimpleStatus.setPromotionStatusProviderWrapper(promotionStatusProviderWrapper);

        final Status resolvedStatus = SimpleStatus.resolveStatus(null, build, null);
        assertFalse(resolvedStatus.isSuccess());
        assertEquals("FAILED", resolvedStatus.toString());
        assertTrue(resolvedStatus.getType().equals(StatusType.FAILED));
        assertTrue(resolvedStatus.getPromotions().isEmpty());
        assertFalse(resolvedStatus.isPromoted());
    }

    @Test
    public void testResolveStatusUnstable() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new UnstableBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild(), null);
        assertTrue(status.isUnstable());
        assertEquals("UNSTABLE", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.UNSTABLE));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusAborted() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MockBuilder(Result.ABORTED));
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild(), null);
        assertTrue(status.isCancelled());
        assertEquals("CANCELLED", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.CANCELLED));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusNotBuilt() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MockBuilder(Result.NOT_BUILT));
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild(), null);
        assertTrue(status.isNotBuilt());
        assertEquals("NOT_BUILT", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.NOT_BUILT));
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusQueued() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.scheduleBuild2(2);
        Status status = SimpleStatus.resolveStatus(project, null, null);
        assertTrue(status.isQueued());
        assertFalse(status.isRunning());
        assertTrue(status.getType().equals(StatusType.QUEUED));
        assertEquals("QUEUED", status.toString());
        jenkins.waitUntilNoActivity();
        status = SimpleStatus.resolveStatus(project, project.getLastBuild(), null);
        assertTrue(status.isSuccess());
        assertTrue(status.getType().equals(StatusType.SUCCESS));
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertFalse(status.isPromoted());
    }

    @Test
    public void testResolveStatusBuilding() throws Exception {
        final OneShotEvent buildStarted = new OneShotEvent();
        final OneShotEvent buildBuilding = new OneShotEvent();

        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                buildStarted.signal();
                buildBuilding.block();
                return true;
            }
        });

        project.scheduleBuild2(0);
        buildStarted.block(); // wait for the build to really start
        Status status = SimpleStatus.resolveStatus(project, project.getFirstBuild(), null);
        assertTrue(status.isRunning());
        buildBuilding.signal();
        jenkins.waitUntilNoActivity();
        assertNotNull(status.getTimestamp());
        assertTrue(status instanceof Running);
        Running running = (Running) status;
        assertFalse(running.getPercentage() == 0);
        assertTrue(running.isRunning());
        assertTrue(status.getType().equals(StatusType.RUNNING));
        assertNotNull(status.toString());
    }

    @Test
    @WithoutJenkins
    public void testBuildingProgressGreaterThanEstimated() {
        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.isBuilding()).thenReturn(true);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, -10000);
        Mockito.when(build.getTimestamp()).thenReturn(calendar);
        Mockito.when(build.getEstimatedDuration()).thenReturn(10l);

        assertEquals(99, ((Running) SimpleStatus.resolveStatus(null, build, null)).getPercentage());
    }

    @Test
    public void testCheckThatAllOnlyQueuedBuildIsResolvedAsQueued() throws Exception {
        FreeStyleProject project1 = jenkins.createFreeStyleProject("project1");
        jenkins.createFreeStyleProject("project2");
        project1.getPublishersList().add(new BuildPipelineTrigger("project2", null));

        jenkins.getInstance().rebuildDependencyGraph();
        Pipeline pipeline = Pipeline.extractPipeline("name", project1);
        jenkins.buildAndAssertSuccess(project1);
        jenkins.buildAndAssertSuccess(project1);
        jenkins.waitUntilNoActivity();

        List<Pipeline> pipelines = pipeline.createPipelineLatest(2, jenkins.getInstance());
        assertEquals(2, pipelines.size());
        assertEquals(StatusType.IDLE, pipelines.get(0).getStages().get(1).getTasks().get(0).getStatus().getType());
        assertEquals(StatusType.IDLE, pipelines.get(1).getStages().get(1).getTasks().get(0).getStatus().getType());

        BuildPipelineView view = new BuildPipelineView("", "", new DownstreamProjectGridBuilder("project1"), "0", false, "");
        project1.setQuietPeriod(3);
        view.triggerManualBuild(1, "project2", "project1");
        pipelines = pipeline.createPipelineLatest(2, jenkins.getInstance());
        assertEquals(2, pipelines.size());
        assertEquals(StatusType.IDLE, pipelines.get(0).getStages().get(1).getTasks().get(0).getStatus().getType());
        assertEquals(StatusType.QUEUED, pipelines.get(1).getStages().get(1).getTasks().get(0).getStatus().getType());

        jenkins.waitUntilNoActivity();
        pipelines = pipeline.createPipelineLatest(2, jenkins.getInstance());
        assertEquals(2, pipelines.size());
        assertEquals(StatusType.IDLE, pipelines.get(0).getStages().get(1).getTasks().get(0).getStatus().getType());
        assertEquals(StatusType.SUCCESS, pipelines.get(1).getStages().get(1).getTasks().get(0).getStatus().getType());
    }
}
