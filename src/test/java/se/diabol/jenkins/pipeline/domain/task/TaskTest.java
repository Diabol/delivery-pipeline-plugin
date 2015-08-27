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

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import hudson.tasks.BuildTrigger;
import hudson.util.OneShotEvent;
import jenkins.model.Jenkins;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.UnstableBuilder;

import se.diabol.jenkins.pipeline.DeliveryPipelineView;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.*;

public class TaskTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetAg() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test");
        jenkins.getInstance().setQuietPeriod(0);

        Task task = Task.getPrototypeTask(project, true);
        assertNotNull(task);
        assertFalse(task.isManual());
        assertFalse(task.isRebuildable());

        Task aggregatedTask = task.getAggregatedTask(null, Jenkins.getInstance());
        assertNotNull(aggregatedTask);
        assertNotNull(task.getLink());
        assertEquals(task.getLink(), aggregatedTask.getLink());

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);


        aggregatedTask = task.getAggregatedTask(build, Jenkins.getInstance());
        assertNotNull(aggregatedTask);
        assertEquals("job/test/1/", aggregatedTask.getLink());


    }

    @Test
    public void testManualTask() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        a.getPublishersList().add(new BuildPipelineTrigger("b", null));
        jenkins.getInstance().rebuildDependencyGraph();

        Task task = Task.getPrototypeTask(b, false);
        assertTrue(task.isManual());

    }

    @Test
    public void testGetLatestRunning() throws Exception {
        final String mockDescription = "some description";

        final OneShotEvent buildStarted = new OneShotEvent();
        final OneShotEvent buildBuilding = new OneShotEvent();

        FreeStyleProject project = jenkins.createFreeStyleProject("test");
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                buildStarted.signal();
                buildBuilding.block();
                return true;
            }
        });
        Task prototype = Task.getPrototypeTask(project, true);

        project.scheduleBuild2(0);
        buildStarted.block(); // wait for the build to really start

        project.getLastBuild().setDescription(mockDescription);
        Task latest = prototype.getLatestTask(jenkins.getInstance(), project.getLastBuild());
        Task aggregated = prototype.getAggregatedTask(project.getLastBuild(), jenkins.getInstance());
        assertEquals("job/test/1/console", latest.getLink());
        assertTrue(latest.getStatus().isRunning());
        assertEquals(mockDescription, aggregated.getDescription());

        assertEquals("job/test/1/console", aggregated.getLink());
        assertTrue(aggregated.getStatus().isRunning());
        buildBuilding.signal();
        jenkins.waitUntilNoActivity();

    }


    @Test
    @Bug(22654)
    public void testTaskNameForMultiConfiguration() throws Exception {
        MatrixProject project = jenkins.createMatrixProject("Multi");
        project.setAxes(new AxisList(new Axis("axis", "foo", "bar")));
        project.addProperty(new PipelineProperty("task", "stage", ""));

        Collection<MatrixConfiguration> configurations = project.getActiveConfigurations();

        for (MatrixConfiguration configuration : configurations) {
            Task task = Task.getPrototypeTask(configuration, true);
            assertEquals("task "  + configuration.getName(), task.getName());

        }
    }


    @Test
    public void testFailedThenQueued() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        jenkins.setQuietPeriod(0);
        a.getPublishersList().add(new BuildPipelineTrigger("b", null));
        b.getBuildersList().add(new FailureBuilder());
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);
        Task task = Task.getPrototypeTask(b, false);
        assertTrue(task.getLatestTask(jenkins.getInstance(), build).getStatus().isIdle());

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline", jenkins.getInstance());
        view.triggerManual("b", "a", "1");
        jenkins.waitUntilNoActivity();
        assertTrue(task.getLatestTask(jenkins.getInstance(), build).getStatus().isFailed());
        jenkins.getInstance().setNumExecutors(0);
        jenkins.getInstance().reload();
        view.triggerManual("b", "a", "1");

        assertTrue(task.getLatestTask(jenkins.getInstance(), build).getStatus().isQueued());

    }

    @Test
    public void testIsRebuildable() throws Exception {
        jenkins.setQuietPeriod(0);
        FreeStyleProject project = jenkins.createFreeStyleProject("project");
        Task task = Task.getPrototypeTask(project, false);
        //IDLE
        assertFalse(task.getLatestTask(jenkins.getInstance(), null).isRebuildable());
        //FAILED
        project.getBuildersList().add(new FailureBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        assertTrue(task.getLatestTask(jenkins.getInstance(), project.getLastBuild()).isRebuildable());
        //UNSTABLE
        project.getBuildersList().clear();
        project.getBuildersList().add(new UnstableBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        assertTrue(task.getLatestTask(jenkins.getInstance(), project.getLastBuild()).isRebuildable());
    }

    @Test
    @Bug(28845)
    public void testIsRebuildableNoPermission() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("A");
        FreeStyleProject b = jenkins.createFreeStyleProject("B");
        b.getBuildersList().add(new FailureBuilder());
        a.getPublishersList().add(new BuildTrigger("B", false));
        jenkins.setQuietPeriod(0);
        jenkins.getInstance().rebuildDependencyGraph();
        FreeStyleBuild firstBuild = jenkins.buildAndAssertSuccess(a);
        jenkins.waitUntilNoActivity();
        assertNotNull(b.getLastBuild());
        assertTrue(b.getLastBuild().getResult().equals(Result.FAILURE));

        jenkins.getInstance().setSecurityRealm(jenkins.createDummySecurityRealm());
        GlobalMatrixAuthorizationStrategy gmas = new GlobalMatrixAuthorizationStrategy();
        gmas.add(Permission.READ, "devel");
        jenkins.getInstance().setAuthorizationStrategy(gmas);

        SecurityContext oldContext = ACL.impersonate(User.get("devel").impersonate());

        Task prototype  = Task.getPrototypeTask(b, false);
        Task task = prototype.getLatestTask(jenkins.getInstance(), firstBuild);
        assertNotNull(task);
        assertFalse(task.isRebuildable());

        SecurityContextHolder.setContext(oldContext);

    }

    @Test
    @Bug(30170)
    public void testTaskName() throws Exception {
        testSimplePipelineTaskNames("Build", "Deploy", "Build", "Deploy", "Build", "Deploy");
    }

    @Test
    public void testTaskNameMacro() throws Exception {
        testSimplePipelineTaskNames("Build ${BUILD_NUMBER}", "Deploy ${BUILD_NUMBER}", "Build ...",
                "Deploy ...", "Build 1", "Deploy 1");
    }

    @Test
    public void testTaskNameMacroOnly() throws Exception {
        testSimplePipelineTaskNames("${BUILD_NUMBER}", "${BUILD_NUMBER}", "...",
                "...", "1", "1");
    }

    private void testSimplePipelineTaskNames(String taskNameA, String taskNameB, String expectedBeforeA,
                                             String expectedBeforeB, String expectedAfterA, String expectedAfterB)
            throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("A");
        FreeStyleProject b = jenkins.createFreeStyleProject("B");
        a.addProperty(new PipelineProperty(taskNameA, "Stage Build", null));
        b.addProperty(new PipelineProperty(taskNameB, "Stage Deploy", null));

        a.getPublishersList().add(new BuildTrigger("B", false));
        jenkins.setQuietPeriod(0);
        jenkins.getInstance().rebuildDependencyGraph();

        Task taskA = Task.getPrototypeTask(a, true).getLatestTask(jenkins.getInstance(), null);
        Task taskB = Task.getPrototypeTask(b, false).getLatestTask(jenkins.getInstance(), null);

        assertEquals(expectedBeforeA, taskA.getName());
        assertEquals(expectedBeforeB, taskB.getName());

        FreeStyleBuild firstBuild = jenkins.buildAndAssertSuccess(a);
        jenkins.waitUntilNoActivity();

        taskA = Task.getPrototypeTask(a, true).getLatestTask(jenkins.getInstance(), firstBuild);
        taskB = Task.getPrototypeTask(b, false).getLatestTask(jenkins.getInstance(), firstBuild);

        assertEquals(expectedAfterA, taskA.getName());
        assertEquals(expectedAfterB, taskB.getName());
    }

}
