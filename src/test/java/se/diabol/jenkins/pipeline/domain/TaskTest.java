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

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.OneShotEvent;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

import static org.junit.Assert.*;

public class TaskTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetAg() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test");
        jenkins.getInstance().setQuietPeriod(0);

        Task task = Task.getPrototypeTask(project);
        assertNotNull(task);

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
    public void testGetLatestRunning() throws Exception {
        final OneShotEvent buildStarted = new OneShotEvent();

        FreeStyleProject project = jenkins.createFreeStyleProject("test");
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                buildStarted.signal();
                Thread.currentThread().wait(3000);
                return true;
            }
        });
        Task prototype = Task.getPrototypeTask(project);

        project.scheduleBuild2(0);
        buildStarted.block(); // wait for the build to really start
        Task latest = prototype.getLatestTask(jenkins.getInstance(), project.getLastBuild());
        Task aggregated = prototype.getAggregatedTask(project.getLastBuild(), jenkins.getInstance());
        assertEquals("job/test/1/console", latest.getLink());
        assertTrue(latest.getStatus().isRunning());

        assertEquals("job/test/1/console", aggregated.getLink());
        assertTrue(aggregated.getStatus().isRunning());


    }


}
