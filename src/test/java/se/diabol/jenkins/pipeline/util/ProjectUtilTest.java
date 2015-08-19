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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hudson.EnvVars;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import hudson.util.ListBoxModel;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import se.diabol.jenkins.pipeline.test.TestUtil;

public class ProjectUtilTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @WithoutJenkins
    @Test
    public void testValidUtilClass() throws Exception {
        TestUtil.assertUtilityClassWellDefined(ProjectUtil.class);
    }

    @Test
    public void testFillAllProjects() throws Exception {
        FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        ListBoxModel list = ProjectUtil.fillAllProjects(jenkins.getInstance());
        assertEquals(2, list.size());
        ListBoxModel.Option option1 = list.get(0);
        assertEquals(build1.getDisplayName(), option1.name);


        ListBoxModel.Option option2 = list.get(1);
        assertEquals(build2.getDisplayName(), option2.name);


    }

    @Test
    public void testGetProjects() throws Exception {
        jenkins.createFreeStyleProject("build-comp1project");
        jenkins.createFreeStyleProject("build-comp1-project");
        jenkins.createFreeStyleProject("build-comp2-project");
        jenkins.createFreeStyleProject("build-comp3-project");
        Map<String, AbstractProject> result = ProjectUtil.getProjects("^build-(.+?)-project");
        assertEquals(3, result.size());
        assertTrue(result.containsKey("comp1"));
        assertTrue(result.containsKey("comp2"));
        assertTrue(result.containsKey("comp3"));

        Map<String, AbstractProject> result2 = ProjectUtil.getProjects("^build-.+?-project");
        assertEquals(0, result2.size());
        Map<String, AbstractProject> result3 = ProjectUtil.getProjects("*");
        assertEquals(0, result3.size());
    }

    @Test
    public void testGetProjectsInFolders() throws Exception {
        jenkins.createFolder("folder1");
        jenkins.createFolder("folder2");

        jenkins.createFreeStyleProject("folder1/project");
        jenkins.createFreeStyleProject("folder1/otherProject");
        jenkins.createFreeStyleProject("folder2/project");
        jenkins.createFreeStyleProject("folder2/otherProject");

        Map<String, AbstractProject> result = ProjectUtil.getProjects("^(project)");
        assertEquals(0, result.size());

        Map<String, AbstractProject> result2 = ProjectUtil.getProjects("^(.+)/project");
        assertEquals(2, result2.size());
    }

    @Test
    public void testGetProjectList() throws Exception {
        jenkins.createFreeStyleProject("p1");
        jenkins.createFreeStyleProject("p2");

        List<AbstractProject> projects = ProjectUtil.getProjectList("p1,p2", jenkins.getInstance(), null);
        assertEquals(2, projects.size());

        projects = ProjectUtil.getProjectList("p1,p2,p3", jenkins.getInstance(), null);
        assertEquals(2, projects.size());

        projects = ProjectUtil.getProjectList("p1,p2,p3", jenkins.getInstance(), new EnvVars());
        assertEquals(2, projects.size());

        projects = ProjectUtil.getProjectList(",,", jenkins.getInstance(), new EnvVars());
        assertEquals(0, projects.size());

    }

    @Test
    public void testRecursiveProjects() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("projectA");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("projectB");
        projectA.getPublishersList().add(new BuildTrigger(projectB.getName(), true));
        projectB.getPublishersList().add(new BuildTrigger(projectA.getName(), true));

        jenkins.getInstance().rebuildDependencyGraph();

        assertEquals(projectA.getUpstreamProjects().get(0), projectB);
        assertEquals(projectB.getUpstreamProjects().get(0), projectA);

        // If there is a cycle dependency, then a stack overflow will be thrown here.
        ProjectUtil.getAllDownstreamProjects(projectA, null);
    }

    @Test
    public void testGetAllDownstreamProjects() {
        Map<String, AbstractProject<?, ?>> result = ProjectUtil.getAllDownstreamProjects(null, null);
        assertTrue(result.isEmpty());
    }
}
