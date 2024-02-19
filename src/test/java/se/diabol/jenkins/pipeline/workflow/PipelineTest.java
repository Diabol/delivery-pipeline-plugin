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
package se.diabol.jenkins.pipeline.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import se.diabol.jenkins.workflow.WorkflowPipelineView;
import se.diabol.jenkins.workflow.model.Pipeline;

public class PipelineTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void simplePipeline() throws Exception {
        final String pipelineName = "Pipeline";
        WorkflowJob pipelineProject = jenkins.jenkins.createProject(WorkflowJob.class, pipelineName);
        pipelineProject.setDefinition(new CpsFlowDefinition("node {stage 'Build' \n stage 'CI' }"));
        WorkflowRun build = pipelineProject.scheduleBuild2(0).get();

        WorkflowPipelineView view = new WorkflowPipelineView(pipelineName);

        Pipeline pipeline = Pipeline.resolve(pipelineProject, build);
        assertNotNull(pipeline);
        assertThat(pipeline.getName(), is(pipelineName));
        assertThat(pipeline.getStages().size(), is(2));
        assertThat(pipeline.getStages().get(0).getName(), is("Build"));
        assertThat(pipeline.getStages().get(1).getName(), is("CI"));
        assertThat(pipeline.isAggregated(), is(false));
        assertNotNull(pipeline.getTimestamp());
        assertNull(pipeline.getContributors());
        assertNotNull(pipeline.getTriggeredBy());
        assertThat(pipeline.getTriggeredBy().size(), is(0));
        assertNotNull(pipeline.getChanges());
        assertThat(pipeline.getChanges().size(), is(0));
    }

    @Test
    public void simplePipelineTasks() throws Exception {
        String pipelineName = "Pipeline";
        WorkflowJob pipelineProject = jenkins.jenkins.createProject(WorkflowJob.class, pipelineName);
        pipelineProject.setDefinition(
                new CpsFlowDefinition("node {\n stage 'Build'\n task('Compile'){}\n stage 'CI'\n task('Deploy'){}\n}"));
        WorkflowRun build = pipelineProject.scheduleBuild2(0).get();

        WorkflowPipelineView view = new WorkflowPipelineView(pipelineName);

        Pipeline pipeline = Pipeline.resolve(pipelineProject, build);
        assertNotNull(pipeline);
        assertThat(pipeline.getStages().size(), is(2));
        assertThat(pipeline.getStages().get(0).getName(), is("Build"));
        assertThat(pipeline.getStages().get(0).getTasks().size(), is(1));
        assertThat(pipeline.getStages().get(0).getTasks().get(0).getName(), is("Compile"));
        assertThat(pipeline.getStages().get(1).getTasks().size(), is(1));
        assertThat(pipeline.getStages().get(1).getTasks().get(0).getName(), is("Deploy"));
    }
}
