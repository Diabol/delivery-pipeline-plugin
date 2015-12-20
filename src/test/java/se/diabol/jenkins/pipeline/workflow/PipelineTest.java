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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import se.diabol.jenkins.workflow.model.Pipeline;

public class PipelineTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void simplePipeline() throws Exception {
        WorkflowJob pipelineProject = jenkins.jenkins.createProject(WorkflowJob.class, "Pipeline");
        pipelineProject.setDefinition(new CpsFlowDefinition("node {stage 'Build' \n stage 'CI' }"));
        //pipelineProject.onCreatedFromScratch();
        WorkflowRun build = pipelineProject.scheduleBuild2(0).get();

        Pipeline pipeline = Pipeline.resolve(pipelineProject, build);
        assertNotNull(pipeline);
        assertEquals(2, pipeline.getStages().size());
        assertEquals("Build", pipeline.getStages().get(0).getName());
        //TODO task assert
    }

    @Test
    public void simplePipelineTasks() throws Exception {
        WorkflowJob pipelineProject = jenkins.jenkins.createProject(WorkflowJob.class, "Pipeline");
        pipelineProject.setDefinition(new CpsFlowDefinition("node {stage 'Build'\n task 'Compile'\n stage 'CI' \n task 'Deploy'}"));
        WorkflowRun build = pipelineProject.scheduleBuild2(0).get();

        Pipeline pipeline = Pipeline.resolve(pipelineProject, build);
        assertNotNull(pipeline);
        assertEquals(2, pipeline.getStages().size());
        assertEquals("Build", pipeline.getStages().get(0).getName());
        assertEquals(1 , pipeline.getStages().get(0).getTasks().size());
        assertEquals("Compile" , pipeline.getStages().get(0).getTasks().get(0).getName());
        assertEquals(1 , pipeline.getStages().get(1).getTasks().size());
        assertEquals("Deploy" , pipeline.getStages().get(1).getTasks().get(0).getName());



        //TODO task assert
    }


}
