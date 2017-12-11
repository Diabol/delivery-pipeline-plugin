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
package se.diabol.jenkins.workflow.model;

import com.gargoylesoftware.htmlunit.Page;
import hudson.cli.BuildCommand;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import se.diabol.jenkins.workflow.WorkflowPipelineView;

import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TaskIntegrationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldHandleNonClosureTaskInClosureStage() throws Exception {
        shouldCreatePipelineAndViewAndSuccessfullyBuildDefinition(
                "node {\n"
                + "stage('Stage1') {\n"
                + "    task 'Task1'\n"
                + "    echo 'Task1'\n"
                + "}\n"
                + "stage('Stage2') {\n"
                + "    task 'Task2'\n"
                + "    echo 'Task2'\n"
                + "}\n"
                + "}"
        );
    }

    @Test
    public void shouldHandleClosureTaskInClosureStage() throws Exception {
        shouldCreatePipelineAndViewAndSuccessfullyBuildDefinition(
                "node {\n"
                + "stage('Stage1') {\n"
                + "    task('Task1') {\n"
                + "        echo 'Task1'\n"
                + "    }\n"
                + "}\n"
                + "stage('Stage2') {\n"
                + "    task('Task2') {\n"
                + "        echo 'Task2'\n"
                + "    }\n"
                + "}\n"
                + "}"
        );
    }

    @Test
    public void shouldHandleNonClosureTaskInNonClosureStage() throws Exception {
        shouldCreatePipelineAndViewAndSuccessfullyBuildDefinition(
                "node {\n"
                + "stage 'Stage1'\n"
                + "task 'Task1'\n"
                + "echo 'Task1'\n"
                + "stage 'Stage2'\n"
                + "task 'Task2'\n"
                + "echo 'Task2'\n"
                + "}"
        );
    }

    @Test
    public void shouldHandleClosureTaskInNonClosureStage() throws Exception {
        shouldCreatePipelineAndViewAndSuccessfullyBuildDefinition(
                "node {\n"
                + "stage 'Stage1'\n"
                + "task('Task1') {\n"
                + "    echo 'Task1'\n"
                + "}\n"
                + "stage 'Stage2'\n"
                + "task('Task2') {\n"
                + "    echo 'Task2'\n"
                + "}\n"
                + "}"
        );
    }

    private void shouldCreatePipelineAndViewAndSuccessfullyBuildDefinition(String script) throws Exception {
        String projectName = "TaskPipeline";
        WorkflowJob pipeline = jenkins.getInstance().createProject(WorkflowJob.class, projectName);
        pipeline.setDefinition(new CpsFlowDefinition(script, true));

        pipeline.scheduleBuild(0, new BuildCommand.CLICause());
        jenkins.waitUntilNoActivity();
        assertThat(pipeline.getLastBuild().getResult(), is(Result.SUCCESS));

        String viewName = "TaskPipelineView";
        WorkflowPipelineView view = new WorkflowPipelineView(viewName);
        view.setProject(projectName);

        jenkins.getInstance().addView(view);

        JenkinsRule.WebClient client = jenkins.createWebClient();

        Page viewPage = client.getPage(new URL(jenkins.getURL(), "/jenkins/view/" + viewName));
        assertThat(viewPage.getWebResponse().getStatusCode(), is(200));

        Page apiPage = client.getPage(new URL(jenkins.getURL(), "/jenkins/view/" + viewName + "/api/json"));
        assertThat(apiPage.getWebResponse().getStatusCode(), is(200));
    }
}
