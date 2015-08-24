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
package se.diabol.jenkins.pipeline.resolver;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import se.diabol.jenkins.pipeline.RelationshipResolver;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ProjectRelationshipResolverTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testNoDownstream() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        RelationshipResolver resolver = new ProjectRelationshipResolver();
        List<AbstractProject> downStreams = resolver.getDownstreamProjects(a);
        assertTrue(downStreams.isEmpty());
    }

    @Test
    public void testHasDownstream() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");
        a.getPublishersList().add(new BuildTrigger("b", false));
        jenkins.getInstance().rebuildDependencyGraph();
        RelationshipResolver resolver = new ProjectRelationshipResolver();
        List<AbstractProject> downStreams = resolver.getDownstreamProjects(a);
        assertFalse(downStreams.isEmpty());
        assertEquals(b, downStreams.get(0));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFirstLastProjects() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("projectA");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("projectB");
        FreeStyleProject projectC = jenkins.createFreeStyleProject("projectC");
        FreeStyleProject projectD = jenkins.createFreeStyleProject("projectD");
        projectA.getPublishersList().add(new BuildTrigger(projectB.getName(), true));
        projectB.getPublishersList().add(new BuildTrigger(projectC.getName(), true));
        projectC.getPublishersList().add(new BuildTrigger(projectD.getName(), true));

        jenkins.getInstance().rebuildDependencyGraph();

        jenkins.getInstance().getExtensionList(RelationshipResolver.class).add(new ProjectRelationshipResolver());
        Map<String, AbstractProject<?, ?>> projects = ProjectUtil.getAllDownstreamProjects(projectB, projectC);
        assertEquals(2, projects.size());
        assertTrue(!projects.containsKey("projectA"));
        assertTrue(projects.containsKey("projectB"));
        assertTrue(projects.containsKey("projectC"));
        assertTrue(!projects.containsKey("projectD"));
    }

}
