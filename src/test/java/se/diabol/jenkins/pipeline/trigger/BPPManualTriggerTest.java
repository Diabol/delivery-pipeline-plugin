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
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BPPManualTriggerTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void triggerManualWithFolders() throws Exception {
        BPPManualTrigger trigger = new BPPManualTrigger();
        MockFolder folder = jenkins.createFolder("folder");
        FreeStyleProject a = folder.createProject(FreeStyleProject.class, "a");
        FreeStyleProject b = folder.createProject(FreeStyleProject.class, "b");

        a.getPublishersList().add(new BuildPipelineTrigger("folder/b", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        jenkins.buildAndAssertSuccess(a);
        assertNotNull(a.getLastBuild());

        trigger.triggerManual(b, a, "1", folder);

        jenkins.waitUntilNoActivity();
        assertNotNull(b.getLastBuild());

    }

    @Test
    @Bug(24392)
    public void triggerManualWithFoldersViewInRoot() throws Exception {
        BPPManualTrigger trigger = new BPPManualTrigger();
        MockFolder folder = jenkins.createFolder("SubFolder");
        FreeStyleProject a = folder.createProject(FreeStyleProject.class, "JobA");
        FreeStyleProject b = folder.createProject(FreeStyleProject.class, "JobB");
        a.getPublishersList().add(new BuildPipelineTrigger("SubFolder/JobB", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        jenkins.buildAndAssertSuccess(a);
        assertNotNull(a.getLastBuild());
        trigger.triggerManual(b, a, "1", jenkins.getInstance());

        jenkins.waitUntilNoActivity();
        assertNotNull(b.getLastBuild());

    }

    @Test(expected=TriggerException.class)
    public void triggerManualWhenProjectNull() throws Exception {
        BPPManualTrigger trigger = new BPPManualTrigger();
        FreeStyleProject b = jenkins.createFreeStyleProject( "b");
        trigger.triggerManual(b, null, "1", Jenkins.getInstance());
        fail("Should throw exception");
    }

    @Test(expected=TriggerException.class)
    public void triggerInvalidBuild() throws Exception {
        BPPManualTrigger trigger = new BPPManualTrigger();
        FreeStyleProject a = jenkins.createFreeStyleProject( "a");
        FreeStyleProject b = jenkins.createFreeStyleProject( "b");
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);

        trigger.triggerManual(b,a , build.getId(), Jenkins.getInstance());
        fail("Should throw exception");
    }

}
