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
package se.diabol.jenkins.pipeline.domain.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import se.diabol.jenkins.pipeline.DeliveryPipelineView;

public class ManualStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testIsManualTriggerAndResolveManualStep() throws Exception {
        final FreeStyleProject upstream = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject downstreamManual = jenkins.createFreeStyleProject("downstreamManual");
        upstream.getPublishersList().add(new BuildPipelineTrigger("downstreamManual", null));
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        projectA.getPublishersList().add(new BuildTrigger("b", false));

        jenkins.getInstance().rebuildDependencyGraph();

        assertTrue(ManualStep.isManualTrigger(downstreamManual));
        assertFalse(ManualStep.isManualTrigger(upstream));

        assertFalse(ManualStep.isManualTrigger(projectA));
        assertFalse(ManualStep.isManualTrigger(projectB));

        assertNull(ManualStep.resolveManualStep(projectA));
        assertNull(ManualStep.resolveManualStep(projectB));
        assertNull(ManualStep.resolveManualStep(upstream));
        ManualStep step = ManualStep.resolveManualStep(downstreamManual);
        assertNotNull(step);
        assertNull(step.getUpstreamId());
        assertFalse(step.isEnabled());
        assertTrue(step.isPermission());
        assertNull(step.getPossibleVersions());
        assertEquals("downstreamManual", step.getUpstreamProject());
    }

    @Test
    public void testGetManualStepLatest() throws Exception {
        FreeStyleProject upstream = jenkins.createFreeStyleProject("upstream");
        FreeStyleProject downstream = jenkins.createFreeStyleProject("downstream");
        upstream.getPublishersList().add(new BuildPipelineTrigger("downstream", null));
        jenkins.getInstance().rebuildDependencyGraph();

        ManualStep step =
                ManualStep.getManualStepLatest(downstream, downstream.getLastBuild(), upstream.getLastBuild());
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertNull(step.getUpstreamId());
        assertFalse(step.isEnabled());
        assertTrue(step.isPermission());
        assertNull(step.getPossibleVersions());

        jenkins.buildAndAssertSuccess(upstream);
        step = ManualStep.getManualStepLatest(downstream, downstream.getLastBuild(), upstream.getLastBuild());
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertEquals("1", step.getUpstreamId());
        assertTrue(step.isEnabled());
        assertTrue(step.isPermission());
        assertNull(step.getPossibleVersions());

        downstream.getBuildersList().add(new FailureBuilder());
        DeliveryPipelineView view = new DeliveryPipelineView("hej", jenkins.getInstance());
        view.triggerManual("downstream", "upstream", "1");
        jenkins.waitUntilNoActivity();

        step = ManualStep.getManualStepLatest(downstream, downstream.getLastBuild(), upstream.getLastBuild());
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertEquals("1", step.getUpstreamId());
        assertTrue(step.isEnabled());
        assertTrue(step.isPermission());
        assertNull(step.getPossibleVersions());
    }

    @Test
    public void testGetManualStepLatestWithFolders() throws Exception {
        MockFolder folder = jenkins.createFolder("folder");
        FreeStyleProject upstream = folder.createProject(FreeStyleProject.class, "upstream");
        FreeStyleProject downstream = folder.createProject(FreeStyleProject.class, "downstream");
        upstream.getPublishersList().add(new BuildPipelineTrigger("folder/downstream", null));
        jenkins.getInstance().rebuildDependencyGraph();


        ManualStep step =
                ManualStep.getManualStepLatest(downstream, downstream.getLastBuild(), upstream.getLastBuild());
        assertNotNull(step);
        assertEquals("folder/upstream", step.getUpstreamProject());
        assertNull(step.getUpstreamId());
        assertFalse(step.isEnabled());
        assertTrue(step.isPermission());
        assertNull(step.getPossibleVersions());

        jenkins.buildAndAssertSuccess(upstream);
        assertNull(downstream.getLastBuild());
        DeliveryPipelineView view = new DeliveryPipelineView("hej", folder);

        view.triggerManual("folder/downstream", "folder/upstream", "1");
        jenkins.waitUntilNoActivity();
        assertNotNull(downstream.getLastBuild());
    }

    @Test
    public void testGetManualStepAggregated() throws Exception {
        final FreeStyleProject upstream = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject downstream = jenkins.createFreeStyleProject("downstream");
        upstream.getPublishersList().add(new BuildPipelineTrigger("downstream", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        ManualStep step = ManualStep.getManualStepAggregated(downstream, upstream);
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertNull(step.getUpstreamId());
        assertFalse(step.isEnabled());
        assertTrue(step.isPermission());
        assertEquals(0, step.getPossibleVersions().size());

        jenkins.buildAndAssertSuccess(upstream);
        step = ManualStep.getManualStepAggregated(downstream, upstream);
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertNull(step.getUpstreamId());
        assertTrue(step.isEnabled());
        assertTrue(step.isPermission());
        assertEquals(1, step.getPossibleVersions().size());

        downstream.getBuildersList().add(new FailureBuilder());
        DeliveryPipelineView view = new DeliveryPipelineView("hej", jenkins.getInstance());
        view.triggerManual("downstream", "upstream", "1");
        jenkins.waitUntilNoActivity();

        step = ManualStep.getManualStepAggregated(downstream, upstream);
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertNull(step.getUpstreamId());
        assertTrue(step.isEnabled());
        assertTrue(step.isPermission());
        assertEquals(1, step.getPossibleVersions().size());

        jenkins.buildAndAssertSuccess(upstream);

        step = ManualStep.getManualStepAggregated(downstream, upstream);
        assertNotNull(step);
        assertEquals("upstream", step.getUpstreamProject());
        assertNull(step.getUpstreamId());
        assertTrue(step.isEnabled());
        assertTrue(step.isPermission());
        assertEquals(2, step.getPossibleVersions().size());
    }

    @Test
    public void getManualStepAggregatedNoTrigger() throws Exception {
        final FreeStyleProject projectA =  jenkins.createFreeStyleProject("a");
        final FreeStyleProject projectB =  jenkins.createFreeStyleProject("b");
        assertNull(ManualStep.getManualStepAggregated(projectA, projectA));

        projectA.getPublishersList().add(new BuildTrigger("b", false));
        jenkins.getInstance().rebuildDependencyGraph();
        assertNull(ManualStep.getManualStepAggregated(projectB, projectA));
    }

    @Test
    public void getManualStepLatestWithMultipleManualTriggers() throws Exception {
        final FreeStyleProject projectA =  jenkins.createFreeStyleProject("A");
        final FreeStyleProject projectB =  jenkins.createFreeStyleProject("B");
        final FreeStyleProject projectC =  jenkins.createFreeStyleProject("C");
        final FreeStyleProject projectD =  jenkins.createFreeStyleProject("D");

        projectA.getPublishersList().add(new BuildPipelineTrigger("B,C", null));
        projectB.getPublishersList().add(new BuildPipelineTrigger("D", null));
        projectC.getPublishersList().add(new BuildPipelineTrigger("D", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        AbstractBuild firstBuild = jenkins.buildAndAssertSuccess(projectA);
        DeliveryPipelineView view = new DeliveryPipelineView("hej", jenkins.getInstance());
        view.triggerManual("C", "A", "1");
        jenkins.waitUntilNoActivity();

        ManualStep step = ManualStep.getManualStepLatest(projectD, null, firstBuild);
        assertNotNull(step);
        assertEquals("1", step.getUpstreamId());
        assertEquals("C", step.getUpstreamProject());
        assertTrue(step.isEnabled());
    }

    @Test
    @Issue("JENKINS-27584")
    public void getManualStepLatestUpstreamDeleted() throws Exception {
        FreeStyleProject projectA =  jenkins.createFreeStyleProject("A");
        FreeStyleProject projectB =  jenkins.createFreeStyleProject("B");
        FreeStyleProject projectC =  jenkins.createFreeStyleProject("C");

        projectA.getPublishersList().add(new BuildTrigger("B", true));
        projectB.getPublishersList().add(new BuildPipelineTrigger("C", null));
        projectC.getBuildersList().add(new FailureBuilder());
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        final AbstractBuild firstBuild = jenkins.buildAndAssertSuccess(projectA);
        jenkins.waitUntilNoActivity();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", jenkins.getInstance());
        view.triggerManual("C", "B", "1");
        jenkins.waitUntilNoActivity();

        ManualStep step = ManualStep.getManualStepLatest(projectC, null, firstBuild);
        assertNotNull(step);

        projectB.getLastBuild().delete();
        assertNull(projectB.getLastBuild());
        step = ManualStep.getManualStepLatest(projectC, projectC.getLastBuild(), firstBuild);
        assertNotNull(step);

    }

    @Test
    @Issue("JENKINS-28937")
    public void testFailure() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        final FreeStyleProject projectC = jenkins.createFreeStyleProject("C");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));
        projectB.getPublishersList().add(new BuildPipelineTrigger("C", null));
        projectB.getBuildersList().add(new FailureBuilder());
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        final AbstractBuild firstBuild = jenkins.buildAndAssertSuccess(projectA);
        jenkins.waitUntilNoActivity();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", jenkins.getInstance());
        view.triggerManual("B", "A", "1");

        jenkins.waitUntilNoActivity();

        ManualStep step = ManualStep.getManualStepLatest(projectC, null, firstBuild);
        assertNotNull(step);
        assertFalse(step.isEnabled());
    }

}
