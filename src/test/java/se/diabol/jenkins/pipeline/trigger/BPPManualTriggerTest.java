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
import static org.junit.Assert.fail;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

public class BPPManualTriggerTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void triggerManualWithFolders() throws Exception {
        final BPPManualTrigger trigger = new BPPManualTrigger();
        MockFolder folder = jenkins.createFolder("folder");
        final FreeStyleProject projectA = folder.createProject(FreeStyleProject.class, "a");
        final FreeStyleProject projectB = folder.createProject(FreeStyleProject.class, "b");

        projectA.getPublishersList().add(new BuildPipelineTrigger("folder/b", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        jenkins.buildAndAssertSuccess(projectA);
        assertNotNull(projectA.getLastBuild());

        trigger.triggerManual(projectB, projectA, "1", folder);

        jenkins.waitUntilNoActivity();
        assertNotNull(projectB.getLastBuild());
    }

    @Test
    @Bug(24392)
    public void triggerManualWithFoldersViewInRoot() throws Exception {
        final BPPManualTrigger trigger = new BPPManualTrigger();
        MockFolder folder = jenkins.createFolder("SubFolder");
        final FreeStyleProject projectA = folder.createProject(FreeStyleProject.class, "JobA");
        final FreeStyleProject projectB = folder.createProject(FreeStyleProject.class, "JobB");
        projectA.getPublishersList().add(new BuildPipelineTrigger("SubFolder/JobB", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        jenkins.buildAndAssertSuccess(projectA);
        assertNotNull(projectA.getLastBuild());
        trigger.triggerManual(projectB, projectA, "1", jenkins.getInstance());

        jenkins.waitUntilNoActivity();
        assertNotNull(projectB.getLastBuild());
    }

    @Test(expected = TriggerException.class)
    public void triggerManualWhenProjectNull() throws Exception {
        BPPManualTrigger trigger = new BPPManualTrigger();
        FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        trigger.triggerManual(projectB, null, "1", Jenkins.getInstance());
        fail("Should throw exception");
    }

    @Test(expected = TriggerException.class)
    public void triggerInvalidBuild() throws Exception {
        BPPManualTrigger trigger = new BPPManualTrigger();
        FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        FreeStyleProject projecB = jenkins.createFreeStyleProject("b");
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(projectA);

        trigger.triggerManual(projecB,projectA , build.getId(), Jenkins.getInstance());
        fail("Should throw exception");
    }

}
