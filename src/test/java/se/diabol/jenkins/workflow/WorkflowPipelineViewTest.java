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
package se.diabol.jenkins.workflow;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.workflow.model.Component;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WorkflowPipelineViewTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldGiveErrorWhenFailingToGetWorkflowJob() throws IOException {
        WorkflowJob pipeline = jenkins.getInstance().createProject(WorkflowJob.class, "job_name");
        pipeline.setDefinition(new CpsFlowDefinition("node { stage 'Build' echo 'Build' }", true));

        WorkflowPipelineView view = new WorkflowPipelineView("Pipeline");
        view.setProject("some_non-existing_job");

        List<Component> pipelines = view.getPipelines();
        assertNotNull(pipelines);
        assertThat(pipelines.size(), is(0));
        assertNotNull(view.getError());
    }

    @Test
    public void shouldResolvePipelineForWorkflowJobWithNoBuilds() throws IOException {
        final String jobName = "Job";
        WorkflowJob pipeline = jenkins.getInstance().createProject(WorkflowJob.class, jobName);
        pipeline.setDefinition(new CpsFlowDefinition("node { stage 'Build' echo 'Build' }", true));

        WorkflowPipelineView view = new WorkflowPipelineView("Pipeline");
        view.setProject(jobName);

        List<Component> pipelines = view.getPipelines();
        assertNotNull(pipelines);
        assertThat(pipelines.size(), is(1));
        Component component = pipelines.get(0);
        assertThat(component.getWorkflowJob(), is(pipeline));
    }

    @Test
    @WithoutJenkins
    public void getDescriptionShouldSetSuperDescriptionIfNotSet() {
        WorkflowPipelineView view = mock(WorkflowPipelineView.class);
        doCallRealMethod().when(view).getDescription();
        doCallRealMethod().when(view).setDescription(anyString());

        String description = view.getDescription();
        verify(view, times(1)).setDescription(anyString());
        assertNull(description);

        String expectedDescription = "some description";
        view.setDescription(expectedDescription);
        assertNotNull(view.getDescription());
        assertThat(view.getDescription(), is(expectedDescription));
        verify(view, times(2)).setDescription(anyString());

        view.getDescription();
        verify(view, times(2)).setDescription(anyString());
    }
}
