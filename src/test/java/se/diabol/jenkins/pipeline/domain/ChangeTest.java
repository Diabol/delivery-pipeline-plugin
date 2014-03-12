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

import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM;
import org.jvnet.hudson.test.JenkinsRule;
import se.diabol.jenkins.pipeline.test.FakeRepositoryBrowserSCM;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChangeTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetChangesNoBrowser() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeChangeLogSCM scm = new FakeChangeLogSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(project);
        AbstractBuild build = project.getLastBuild();
        List<Change> changes = Change.getChanges(build);
        assertNotNull(changes);
        assertEquals(1, changes.size());
        Change change = changes.get(0);
        assertEquals("Fixed bug", change.getMessage());
        assertEquals("test-user", change.getAuthor().getName());
        assertNull(change.getCommitId());
        assertNull(change.getChangeLink());
    }

    @Test
    public void testGetChangesWithBrowser() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user").withMsg("Fixed bug");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(project);
        AbstractBuild build = project.getLastBuild();
        List<Change> changes = Change.getChanges(build);
        assertNotNull(changes);
        assertEquals(1, changes.size());
        Change change = changes.get(0);
        assertEquals("Fixed bug", change.getMessage());
        assertEquals("test-user", change.getAuthor().getName());
        assertNull(change.getCommitId());
        assertEquals("http://somewhere.com/test-user", change.getChangeLink());
    }





}
