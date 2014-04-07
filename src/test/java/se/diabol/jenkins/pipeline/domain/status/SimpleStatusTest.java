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

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.OneShotEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.*;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SimpleStatusTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testResolveStatusIdle() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        Status status = SimpleStatus.resolveStatus(project, null);
        assertTrue(status.isIdle());
        assertEquals("IDLE", status.toString());
        assertEquals(-1, status.getLastActivity());
        assertEquals(-1, status.getDuration());
        assertNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.IDLE));
    }

    @Test
    public void testResolveStatusDisabled() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.makeDisabled(true);
        Status status = SimpleStatus.resolveStatus(project, null);
        assertTrue(status.isDisabled());
        assertEquals("DISABLED", status.toString());
        assertEquals(-1, status.getLastActivity());
        assertEquals(-1, status.getDuration());
        assertNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.DISABLED));
    }

    @Test
    public void testResolveStatusSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        jenkins.buildAndAssertSuccess(project);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isSuccess());
        assertEquals("SUCCESS", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.SUCCESS));
    }

    @Test
    public void testResolveStatusFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new FailureBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isFailed());
        assertEquals("FAILED", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.FAILED));
    }


    @Test
    public void testResolveStatusUnstable() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new UnstableBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isUnstable());
        assertEquals("UNSTABLE", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.UNSTABLE));
    }



    @Test
    public void testResolveStatusAborted() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MockBuilder(Result.ABORTED));
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = SimpleStatus.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isCancelled());
        assertEquals("CANCELLED", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
        assertTrue(status.getType().equals(StatusType.CANCELLED));
    }

    @Test
    public void testResolveStatusNotBuilt() throws Exception {
        //Result.NOT_BUILT should never occur for a build, just for a module within a maven build.
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MockBuilder(Result.NOT_BUILT));
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        try {
            SimpleStatus.resolveStatus(project, project.getLastBuild());
            fail("Should throw exception here");
        } catch (IllegalStateException e) {
        }
    }


    @Test
    public void testResolveStatusQueued() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.scheduleBuild2(2);
        Status status = SimpleStatus.resolveStatus(project, null);
        assertTrue(status.isQueued());
        assertFalse(status.isRunning());
        assertTrue(status.getType().equals(StatusType.QUEUED));
        assertEquals("QUEUED", status.toString());
        jenkins.waitUntilNoActivity();
        status = SimpleStatus.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isSuccess());
        assertTrue(status.getType().equals(StatusType.SUCCESS));
        assertEquals(project.getLastBuild().getDuration(), status.getDuration());
        assertNotNull(status.getTimestamp());
    }

    @Test
    public void testResolveStatusBuilding() throws Exception {
        final OneShotEvent buildStarted = new OneShotEvent();

        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                buildStarted.signal();
                Thread.currentThread().wait(1000);
                return true;
            }
        });

        project.scheduleBuild2(0);
        buildStarted.block(); // wait for the build to really start
        Status status = SimpleStatus.resolveStatus(project, project.getFirstBuild());
        jenkins.waitUntilNoActivity();
        assertTrue(status.isRunning());
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

        assertEquals(99, ((Running)SimpleStatus.resolveStatus(null, build)).getPercentage());

    }

}
