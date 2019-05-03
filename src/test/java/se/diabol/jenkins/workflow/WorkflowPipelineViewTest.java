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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;

import com.gargoylesoftware.htmlunit.Page;

import hudson.cli.BuildCommand;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import se.diabol.jenkins.pipeline.DeliveryPipelineView;
import se.diabol.jenkins.workflow.model.Component;

public class WorkflowPipelineViewTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void shouldSubmitForm() throws Exception {
        WorkflowPipelineView view = new WorkflowPipelineView("viewName");
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getSubmittedForm()).thenReturn(new JSONObject());
        view.submit(request);
        verify(request, times(1)).bindJSON(view, new JSONObject());
        verify(request, times(1)).bindJSONToList(WorkflowPipelineView.ComponentSpec.class, null);
    }

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
    @Issue("JENKINS-43797")
    public void authentication() throws Exception {
        WorkflowJob pipeline = jenkins.getInstance().createProject(WorkflowJob.class, "Test");

        pipeline.setDefinition(new CpsFlowDefinition("node {\nstage 'Stage 1'\necho 'Hello World 1'\nstage 'Stage 2'\necho 'Hello World 2'\n}", true));

        pipeline.scheduleBuild(0, new BuildCommand.CLICause());
        jenkins.waitUntilNoActivity();

        WorkflowPipelineView view = new WorkflowPipelineView("Pipeline");
        view.setProject("Test");

        jenkins.getInstance().addView(view);

        jenkins.getInstance().setSecurityRealm(jenkins.createDummySecurityRealm());
        GlobalMatrixAuthorizationStrategy gmas = new GlobalMatrixAuthorizationStrategy();
        gmas.add(Permission.READ, "devel");

        pipeline.getLastBuild().getExecution().getAuthentication().getCredentials();

        jenkins.getInstance().setAuthorizationStrategy(gmas);

        JenkinsRule.WebClient client = jenkins.createWebClient();

        client.login("devel", "devel");

        Page pageView = client.getPage(new URL(jenkins.getURL(), "/jenkins/view/Pipeline"));
        assertThat(pageView.getWebResponse().getStatusCode(), is(200));

        Page pageApi = client.getPage(new URL(jenkins.getURL(), "/jenkins/view/Pipeline/api/json"));
        assertThat(pageApi.getWebResponse().getStatusCode(), is(200));
    }

    @Test
    @Issue("JENKINS-47529")
    public void shouldMigrateLegacyProjectConfigurationToComponentSpec() throws IOException {
        String jobName = "PipelineProject";
        WorkflowJob job = jenkins.getInstance().createProject(WorkflowJob.class, jobName);
        WorkflowPipelineView view = new WorkflowPipelineView("view");
        view.setProject(jobName);

        assertThat(view.getProject(), is(jobName));
        assertThat(view.getComponentSpecs().size(), is(0));

        view.getPipelines();

        assertNull(view.getProject());
        assertThat(view.getComponentSpecs().size(), is(1));
        assertThat(view.getComponentSpecs().get(0).getJob(), is(jobName));
    }

    @Test
    @WithoutJenkins
    public void getDescriptionShouldSetSuperDescriptionIfNotSet() {
        WorkflowPipelineView view = mock(WorkflowPipelineView.class);
        doCallRealMethod().when(view).getDescription();
        doCallRealMethod().when(view).setDescription(or(Mockito.isNull(), anyString()));

        String description = view.getDescription();
        verify(view, times(1)).setDescription(or(Mockito.isNull(), anyString()));
        assertNull(description);

        String expectedDescription = "some description";
        view.setDescription(expectedDescription);
        assertNotNull(view.getDescription());
        assertThat(view.getDescription(), is(expectedDescription));
        verify(view, times(2)).setDescription(or(Mockito.isNull(), anyString()));

        view.getDescription();
        verify(view, times(2)).setDescription(or(Mockito.isNull(), anyString()));
    }

    @Test
    @WithoutJenkins
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void shouldValidateCheckUpdateInterval() {
        WorkflowPipelineView.DescriptorImpl descriptor = new WorkflowPipelineView.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckUpdateInterval("1").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckUpdateInterval("3").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval(null).kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval("0").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckUpdateInterval("3a").kind);
    }

    @Test
    @WithoutJenkins
    public void shouldHaveDefaults() {
        WorkflowPipelineView view = new WorkflowPipelineView("name");
        assertThat(view.getNoOfPipelines(), is(3));
        assertThat(view.getNoOfColumns(), is(1));
        assertThat(view.getUpdateInterval(), is(2));
        assertNotNull(view.getComponentSpecs());
        assertThat(view.getComponentSpecs().size(), is(0));
        assertNull(view.getDescription());
        assertThat(view.isShowChanges(), is(false));
        assertThat(view.isAllowPipelineStart(), is(false));
        assertThat(view.getTheme(), is("default"));
        assertEquals(-1, view.getMaxNumberOfVisiblePipelines());
        assertThat(view.isLinkToConsoleLog(), is(false));
    }

    @Test
    @WithoutJenkins
    public void testSetDefaultThemeIfNull() {
        WorkflowPipelineView view = new WorkflowPipelineView("name");
        view.setTheme(null);
        assertEquals(DeliveryPipelineView.DEFAULT_THEME, view.getTheme());
    }

    @Test
    public void shouldSetMaxNumberOfVisiblePipelines() throws Exception {
        final String jobName = "workflowJob";
        jenkins.getInstance().createProject(WorkflowJob.class, jobName);
        List<WorkflowPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new WorkflowPipelineView.ComponentSpec("Comp", jobName));
        specs.add(new WorkflowPipelineView.ComponentSpec("Comp1", jobName));
        WorkflowPipelineView view = new WorkflowPipelineView("Pipeline");
        view.setComponentSpecs(specs);
        view.setMaxNumberOfVisiblePipelines(1);
        jenkins.getInstance().addView(view);
        List<Component> pipelines = view.getPipelines();
        assertEquals(1, pipelines.size());
        assertNull(view.getError());
    }

    @Test
    public void shouldReturnAllJobsWhenMaxNumberOfPipelinesNotSet() throws Exception {
        final String jobName = "pipelineJobName";
        jenkins.getInstance().createProject(WorkflowJob.class, jobName);
        List<WorkflowPipelineView.ComponentSpec> specs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            specs.add(new WorkflowPipelineView.ComponentSpec("Comp" + i, jobName));
        }
        WorkflowPipelineView view = new WorkflowPipelineView("Pipeline");
        view.setComponentSpecs(specs);
        jenkins.getInstance().addView(view);
        List<Component> pipelines = view.getPipelines();
        assertEquals(specs.size(), pipelines.size());
        assertNull(view.getError());
    }
}
