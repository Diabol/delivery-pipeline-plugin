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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.ItemGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import se.diabol.jenkins.pipeline.domain.PipelineException;
import se.diabol.jenkins.workflow.api.Run;

import java.io.IOException;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class WorkflowApiTest {

    private WorkflowApi workflowApi = mock(WorkflowApi.class);

    @Before
    public void setup() throws IOException, PipelineException {
        when(workflowApi.lastFinishedRunFor(anyString(), any(ItemGroup.class))).thenCallRealMethod();
    }

    @Test
    public void shouldGetLastFinishedRunForJob() throws PipelineException {
        Run inProgressRun = new Run(null, null, "IN_PROGRESS", null, null, null, null);
        Run pausedRun = new Run(null, null, "PAUSED_PENDING_INPUT", null, null, null, null);
        Run finishedRun = new Run("5", "#5", "SUCCESS", null, null, null, null);
        Run earlierFinishedRun = new Run("4", "#4", "SUCCESS", null, null, null, null);
        when(workflowApi.getRunsFor(anyString(), any(ItemGroup.class)))
                .thenReturn(Arrays.asList(inProgressRun, pausedRun, finishedRun, earlierFinishedRun));

        Run run = workflowApi.lastFinishedRunFor("Test Workflow", Mockito.mock(ItemGroup.class));
        assertThat(run.id, is(finishedRun.id));
        assertThat(run.name, is(finishedRun.name));
        assertThat(run.status, is(finishedRun.status));
    }

    @Test
    public void shouldNotGetLastFinishedRunForJobIfOnlyInProgressOrPausedJobsExist() throws PipelineException {
        Run inProgressRun = new Run(null, null, "IN_PROGRESS", null, null, null, null);
        Run pausedRun = new Run(null, null, "PAUSED_PENDING_INPUT", null, null, null, null);
        when(workflowApi.getRunsFor(anyString(), any(ItemGroup.class)))
                .thenReturn(Arrays.asList(inProgressRun, pausedRun));

        assertThat(workflowApi.lastFinishedRunFor("Test Workflow", Mockito.mock(ItemGroup.class)), nullValue());
    }
}
