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
package se.diabol.jenkins.pipeline.util;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.pipeline.test.TestUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildUtilTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testValidUtilClass() throws Exception {
        TestUtil.assertUtilityClassWellDefined(BuildUtil.class);
    }


    @Test
    public void testFirstUpstreamBuildFirstProjectHasUpstreamJob() throws Exception {
        FreeStyleProject upstream = jenkins.createFreeStyleProject("upstream");
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject pack = jenkins.createFreeStyleProject("package");
        upstream.getPublishersList().add(new BuildTrigger("build", false));
        build.getPublishersList().add(new BuildTrigger("package", false));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(upstream);
        jenkins.waitUntilNoActivity();

        assertNotNull(upstream.getLastBuild());
        assertNotNull(build.getLastBuild());
        assertNotNull(pack.getLastBuild());

        assertEquals(build.getLastBuild(), BuildUtil.getFirstUpstreamBuild(pack.getLastBuild(), build));

    }

    @Test
    @WithoutJenkins
    public void testGetFirstUpstreamBuildNull() {
        assertNull(BuildUtil.getFirstUpstreamBuild(null, null));
    }

    @Test
    public void testGetUpstreamBuildProjectRenamed() {
        AbstractBuild build = mock(AbstractBuild.class);
        List<CauseAction> causeActions = new ArrayList<CauseAction>();
        Cause.UpstreamCause cause = mock(Cause.UpstreamCause.class);
        when(cause.getUpstreamProject()).thenReturn("thisprojectdontexists");
        causeActions.add(new CauseAction(cause));
        when(build.getActions(CauseAction.class)).thenReturn(causeActions);

        assertNull(BuildUtil.getUpstreamBuild(build));

    }

    @Test
    public void testEquals() throws Exception {
        MockFolder folder1 = jenkins.createFolder("Folder1");
        MockFolder folder2 = jenkins.createFolder("Folder2");
        FreeStyleProject job1 = folder1.createProject(FreeStyleProject.class, "a");
        FreeStyleProject job2 = folder2.createProject(FreeStyleProject.class, "a");
        AbstractBuild build1 = jenkins.buildAndAssertSuccess(job1);
        AbstractBuild build2 = jenkins.buildAndAssertSuccess(job2);
        assertFalse(BuildUtil.equals(build1, build2));
        assertTrue(BuildUtil.equals(build1, build1));
        AbstractBuild build3 = jenkins.buildAndAssertSuccess(job1);
        assertFalse(BuildUtil.equals(build1, build3));
        assertFalse(BuildUtil.equals(null, null));


    }





}
