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
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.pipeline.test.FakeRepositoryBrowserSCM;

import java.util.Set;

import static org.junit.Assert.*;

public class UserInfoTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testGetContributorsEmpty() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new Cause.UserIdCause());
        jenkins.waitUntilNoActivity();
        Set<UserInfo> contributors = UserInfo.getContributors(project.getLastBuild());
        assertEquals(0, contributors.size());
    }

    @Test
    public void testGetContributorsMultiple() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("build");
        FakeRepositoryBrowserSCM scm = new FakeRepositoryBrowserSCM();
        scm.addChange().withAuthor("test-user1").withMsg("Fixed bug1");
        scm.addChange().withAuthor("test-user2").withMsg("Fixed bug1");
        project.setScm(scm);
        jenkins.setQuietPeriod(0);
        project.scheduleBuild(new Cause.UserIdCause());
        jenkins.waitUntilNoActivity();
        Set<UserInfo> contributors = UserInfo.getContributors(project.getLastBuild());
        assertEquals(2, contributors.size());
        assertTrue(contributors.contains(new UserInfo("test-user1", null)));
        assertTrue(contributors.contains(new UserInfo("test-user2", null)));
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

        Set<UserInfo> contributors = UserInfo.getContributors(project.getLastBuild());
        assertEquals(1, contributors.size());
        UserInfo user = contributors.iterator().next();
        assertEquals("test-user", user.getName());
        assertNotNull(user.getUrl());

        assertTrue(contributors.contains(new UserInfo("test-user", null)));
    }

    @Test
    @WithoutJenkins
    @SuppressWarnings("all")
    public void testEqualsHashCode() {
        UserInfo userInfo1 = new UserInfo("name", null);
        assertTrue(userInfo1.equals(userInfo1));
        UserInfo userInfo2 = new UserInfo("name", "http://nowhere.com");
        assertTrue(userInfo2.equals(userInfo1));

        assertFalse(userInfo2.equals(null));
        assertFalse(userInfo2.equals("name"));


        UserInfo userInfo3 = new UserInfo("name1", "http://nowhere.com");
        assertEquals(userInfo1.hashCode(), userInfo2.hashCode());
        assertNotEquals(userInfo1.hashCode(), userInfo3.hashCode());
        assertNotEquals(userInfo1, userInfo3);
    }

}
