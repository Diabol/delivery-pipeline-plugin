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
package se.diabol.jenkins.pipeline.domain;

import hudson.model.*;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.pipeline.test.FakeRepositoryBrowserSCM;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TriggerTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetTriggeredBy() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new Cause.UserIdCause());
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_MANUAL, triggeredBy.iterator().next().getType());
    }

    @Test
    public void testGetTriggeredByWithChangeLog() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new Cause.UserIdCause());
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_MANUAL, triggeredBy.iterator().next().getType());
    }

    @Test
    public void testGetTriggeredByWithNoUserIdCause() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(project);
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_UNKNOWN, triggeredBy.iterator().next().getType());
    }

    @Test
    public void testGetTriggeredByTimer() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        jenkins.setQuietPeriod(0);
        project.setScm(scm);
        project.scheduleBuild(new TimerTrigger.TimerTriggerCause());
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_TIMER, triggeredBy.iterator().next().getType());
    }

    @Test
    public void testGetTriggeredBySCMChange() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new SCMTrigger.SCMTriggerCause("SCM"));
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_SCM, triggeredBy.iterator().next().getType());
    }

    @Test
    @Bug(22611)
    public void testGetTriggeredByMultipleSCMChange() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        CauseAction action = new CauseAction(new SCMTrigger.SCMTriggerCause(""));
        action.getCauses().add(new SCMTrigger.SCMTriggerCause(""));
        action.getCauses().add(new SCMTrigger.SCMTriggerCause(""));
        action.getCauses().add(new SCMTrigger.SCMTriggerCause(""));


        project.scheduleBuild(0, null, action);
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_SCM, triggeredBy.iterator().next().getType());
    }


    @Test
    public void testGetTriggeredByRemoteCause() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new Cause.RemoteCause("localhost", "Remote"));
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_REMOTE, triggeredBy.iterator().next().getType());
    }

    @Test
    public void testGetTriggeredByDeeplyNestedUpstreamCause() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new Cause.UpstreamCause.DeeplyNestedUpstreamCause());
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_UPSTREAM, triggeredBy.iterator().next().getType());
    }

    @Test
    public void testGetTriggeredByUpStreamJob() throws Exception {
        FreeStyleProject upstream = jenkins.createFreeStyleProject("up");
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(upstream);
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);
        project.scheduleBuild(new Cause.UpstreamCause((Run)upstream.getLastBuild()));
        jenkins.waitUntilNoActivity();
        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_UPSTREAM, triggeredBy.iterator().next().getType());
        assertEquals("upstream project up build #1", triggeredBy.iterator().next().getDescription());
    }

    @Test
    @Ignore("Deprecated as of Jenkins 1.548 - upstream descriptions have changed")
    public void testGetTriggeredByUpStreamJobNotExists() throws Exception {
        FreeStyleProject upstream = jenkins.createFreeStyleProject("up");
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(upstream);
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        project.scheduleBuild(new Cause.UpstreamCause((Run)upstream.getLastBuild()));
        jenkins.waitUntilNoActivity();
        upstream.delete();

        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_UPSTREAM, triggeredBy.iterator().next().getType());
        assertEquals("upstream project", triggeredBy.iterator().next().getDescription());
    }

    @Test
    public void testGetTriggeredByUpStreamJobBuildNotExist() throws Exception {
        FreeStyleProject upstream = jenkins.createFreeStyleProject("up");
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(upstream);
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        project.scheduleBuild(new Cause.UpstreamCause((Run)upstream.getLastBuild()));
        jenkins.waitUntilNoActivity();
        upstream.getLastBuild().delete();

        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_UPSTREAM, triggeredBy.iterator().next().getType());
        assertEquals("upstream project up", triggeredBy.iterator().next().getDescription());
    }


    @Test
    public void testGetTriggeredByWithCulprits() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user-fail").withMsg("Fixed bug");
        scm.addChange().withAuthor("test-user-fail2").withMsg("Fixed bug");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        project.getBuildersList().add(new FailureBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();

        scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);

        project.scheduleBuild(new Cause.UserIdCause());
        jenkins.waitUntilNoActivity();

        AbstractBuild build = project.getLastBuild();

        assertEquals(3, build.getCulprits().size());

        List<Trigger> triggeredBy = Trigger.getTriggeredBy(project.getLastBuild());
        assertEquals(1, triggeredBy.size());
        assertEquals(Trigger.TYPE_MANUAL, triggeredBy.iterator().next().getType());
    }

    @Test
    @WithoutJenkins
    public void testHashcodeEquals() {
       Trigger trigger1 = new Trigger(Trigger.TYPE_MANUAL, "manual trigger");
        Trigger trigger2 = new Trigger(Trigger.TYPE_MANUAL, "manual trigger");
        Trigger trigger3 = new Trigger(Trigger.TYPE_MANUAL, "manual");
        Trigger trigger4 = new Trigger(Trigger.TYPE_SCM, "manual");
        assertEquals(trigger1, trigger1);
        assertEquals(trigger1, trigger2);
        assertEquals(trigger1.hashCode(), trigger2.hashCode());
        assertNotEquals(trigger1, trigger3);
        assertNotEquals(trigger3, trigger4);
        assertNotEquals(trigger3, null);
        assertNotEquals(trigger3, "");
        assertNotEquals(trigger1.hashCode(), trigger3.hashCode());
    }

}
