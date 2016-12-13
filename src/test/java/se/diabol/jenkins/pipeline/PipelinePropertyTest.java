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
package se.diabol.jenkins.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import hudson.model.AutoCompletionCandidates;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.WithoutJenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class PipelinePropertyTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDoCheckStageName() {
        PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckStageName("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckStageName(" ").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckStageName("Stage").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckStageName(null).kind);
    }

    @Test
    @WithoutJenkins
    public void testDoCheckTaskName() {
        PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckTaskName("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckTaskName(" ").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckTaskName("Task").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckTaskName(null).kind);
    }

    @Test
    @WithoutJenkins
    public void testIsApplicable() {
        PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        assertTrue(descriptor.isApplicable(FreeStyleProject.class));
    }

    @Test
    @WithoutJenkins
    public void testNewInstanceEmpty() throws Exception {
        final PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        when(request.getParameter("taskName")).thenReturn("");
        when(request.getParameter("stageName")).thenReturn("");
        when(request.getParameter("descriptionTemplate")).thenReturn("");
        when(request.getParameter("enabled")).thenReturn("on");
        assertNull(descriptor.newInstance(request, null));
    }

    @Test
    @WithoutJenkins
    public void testNewInstanceNull() throws Exception {
        final PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        when(request.getParameter("taskName")).thenReturn(null);
        when(request.getParameter("stageName")).thenReturn(null);
        when(request.getParameter("enabled")).thenReturn("on");
        assertNull(descriptor.newInstance(request, null));
    }

    @Test
    @WithoutJenkins
    public void testNewInstanceTaskNull() throws Exception {
        PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        when(request.getParameter("taskName")).thenReturn(null);
        when(request.getParameter("stageName")).thenReturn("Stage");
        when(request.getParameter("enabled")).thenReturn("on");
        PipelineProperty property = descriptor.newInstance(request, null);
        assertNotNull(property);
        assertNull(property.getTaskName());
        assertEquals("Stage", property.getStageName());
    }

    @Test
    @WithoutJenkins
    public void testNewInstanceTaskNullDisabled() throws Exception {
        PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        when(request.getParameter("enabled")).thenReturn(null);
        assertNull(descriptor.newInstance(request, null));
    }

    @Test
    @WithoutJenkins
    public void testNewInstanceBothSet() throws Exception {
        PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        when(request.getParameter("taskName")).thenReturn("Task");
        when(request.getParameter("stageName")).thenReturn("Stage");
        when(request.getParameter("enabled")).thenReturn("on");
        PipelineProperty property = descriptor.newInstance(request, null);
        assertNotNull(property);
        assertEquals("Task", property.getTaskName());
        assertEquals("Stage", property.getStageName());
    }

    @Test
    public void testDoAutoCompleteStageName() throws Exception {
        final PipelineProperty.DescriptorImpl descriptor = new PipelineProperty.DescriptorImpl();
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        jenkins.createFreeStyleProject("build3");
        build2.addProperty(new PipelineProperty());
        build.addProperty(new PipelineProperty("Build", "Build", ""));

        AutoCompletionCandidates c1 = descriptor.doAutoCompleteStageName("B");
        assertEquals(c1.getValues().size(), 1);

        AutoCompletionCandidates c2 = descriptor.doAutoCompleteStageName("A");
        assertEquals(c2.getValues().size(), 0);

        AutoCompletionCandidates c3 = descriptor.doAutoCompleteStageName(null);
        assertEquals(c3.getValues().size(), 0);
    }

    @Test
    public void testGetStageNames() throws Exception {
        MockFolder folder = jenkins.createFolder("folder");
        final FreeStyleProject build1 = folder.createProject(FreeStyleProject.class, "build1");
        final FreeStyleProject build2 = folder.createProject(FreeStyleProject.class, "build2");

        Set<String> stageNames = PipelineProperty.getStageNames();
        assertNotNull(stageNames);
        assertEquals(0, stageNames.size());
        build1.addProperty(new PipelineProperty(null, "Build", ""));
        build2.addProperty(new PipelineProperty(null, "QA", ""));

        stageNames = PipelineProperty.getStageNames();
        assertNotNull(stageNames);
        assertEquals(2, stageNames.size());
        assertTrue(stageNames.contains("Build"));
        assertTrue(stageNames.contains("QA"));
    }


}
