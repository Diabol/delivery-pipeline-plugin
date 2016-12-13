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
package se.diabol.jenkins.pipeline.trigger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

public class BPPManualTriggerResolverTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetTriggerNoRelation() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        assertNull(new BPPManualTriggerResolver().getManualTrigger(projectA, projectB));
    }

    @Test
    public void testGetTriggerBPPManualTrigger() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        final FreeStyleProject projectC = jenkins.createFreeStyleProject("c");
        jenkins.createFreeStyleProject("d");

        projectA.getPublishersList().add(new BuildPipelineTrigger("b", null));
        projectC.getPublishersList().add(new BuildPipelineTrigger("d", null));

        jenkins.getInstance().rebuildDependencyGraph();

        assertNotNull(new BPPManualTriggerResolver().getManualTrigger(projectB, projectA));
        assertNotNull(new BPPManualTriggerResolver().getManualTrigger(projectB, projectA));
        assertNull(new BPPManualTriggerResolver().getManualTrigger(projectB, projectC));
    }

    @Test
    public void testGetTriggerBPPManualTriggerFolders() throws Exception {
        MockFolder folder = jenkins.createFolder("folder");
        FreeStyleProject projectA = folder.createProject(FreeStyleProject.class, "a");
        FreeStyleProject projectB = folder.createProject(FreeStyleProject.class, "b");
        projectA.getPublishersList().add(new BuildPipelineTrigger("folder/b", null));
        jenkins.getInstance().rebuildDependencyGraph();

        assertNotNull(new BPPManualTriggerResolver().getManualTrigger(projectB, projectA));
    }

    @Test
    public void testIsManualTriggerMultipleUpstreams() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        final FreeStyleProject projectC = jenkins.createFreeStyleProject("c");

        projectA.getPublishersList().add(new BuildTrigger("c", true));
        projectB.getPublishersList().add(new BuildPipelineTrigger("c", null));

        jenkins.getInstance().rebuildDependencyGraph();

        assertTrue(new BPPManualTriggerResolver().isManualTrigger(projectC));
    }
}
