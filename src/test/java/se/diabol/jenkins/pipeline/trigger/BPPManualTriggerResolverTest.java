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

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BPPManualTriggerResolverTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetTriggerNoRelation() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        assertNull(new BPPManualTriggerResolver().getManualTrigger(a, b));
    }

    @Test
    public void testGetTriggerBPPManualTrigger() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        FreeStyleProject c = jenkins.createFreeStyleProject("c");
        jenkins.createFreeStyleProject("d");


        a.getPublishersList().add(new BuildPipelineTrigger("b", null));
        c.getPublishersList().add(new BuildPipelineTrigger("d", null));

        jenkins.getInstance().rebuildDependencyGraph();

        assertNotNull(new BPPManualTriggerResolver().getManualTrigger(b, a));
        assertNotNull(new BPPManualTriggerResolver().getManualTrigger(b, a));
        assertNull(new BPPManualTriggerResolver().getManualTrigger(b, c));
    }

    @Test
    public void testGetTriggerBPPManualTriggerFolders() throws Exception {
        MockFolder folder = jenkins.createFolder("folder");
        FreeStyleProject a = folder.createProject(FreeStyleProject.class, "a");
        FreeStyleProject b = folder.createProject(FreeStyleProject.class, "b");
        a.getPublishersList().add(new BuildPipelineTrigger("folder/b", null));
        jenkins.getInstance().rebuildDependencyGraph();

        assertNotNull(new BPPManualTriggerResolver().getManualTrigger(b, a));
    }

    @Test
    public void testIsManualTriggerMultipleUpstreams() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        FreeStyleProject c = jenkins.createFreeStyleProject("c");

        a.getPublishersList().add(new BuildTrigger("c", true));
        b.getPublishersList().add(new BuildPipelineTrigger("c", null));

        jenkins.getInstance().rebuildDependencyGraph();

        assertTrue(new BPPManualTriggerResolver().isManualTrigger(c));
    }
}
