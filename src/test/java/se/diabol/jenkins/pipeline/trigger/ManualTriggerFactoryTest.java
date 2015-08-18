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
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.pipeline.test.TestUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ManualTriggerFactoryTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();


    @Test
    @WithoutJenkins
    public void testValidUtilClass() throws Exception {
        TestUtil.assertUtilityClassWellDefined(ManualTriggerFactory.class);
    }


    @Test
    public void testNotFound() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        assertNull(ManualTriggerFactory.getManualTrigger(b, a));
    }

    @Test
    public void testFound() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        a.getPublishersList().add(new BuildPipelineTrigger("b", null));
        jenkins.getInstance().rebuildDependencyGraph();
        assertNotNull(ManualTriggerFactory.getManualTrigger(b, a));
    }

}
