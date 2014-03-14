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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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

}
