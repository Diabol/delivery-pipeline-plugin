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

import com.google.api.client.http.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.diabol.jenkins.workflow.api.Run;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowApiTest {

    private final String listOfRunsResponseJson = "[{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/5/wfapi/describe\"}},\"id\":\"5\",\"name\":\"#5\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416676325,\"endTimeMillis\":1465416682442,\"durationMillis\":6070,\"queueDurationMillis\":47,\"pauseDurationMillis\":0,\"stages\":[{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/5/execution/node/5/wfapi/describe\"}},\"id\":\"5\",\"name\":\"Build\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416676372,\"durationMillis\":2014,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/5/execution/node/8/wfapi/describe\"}},\"id\":\"8\",\"name\":\"Test\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416678386,\"durationMillis\":2036,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/5/execution/node/11/wfapi/describe\"}},\"id\":\"11\",\"name\":\"Deploy\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416680422,\"durationMillis\":2020,\"pauseDurationMillis\":0}]},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/4/wfapi/describe\"}},\"id\":\"4\",\"name\":\"#4\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416626078,\"endTimeMillis\":1465416632196,\"durationMillis\":6062,\"queueDurationMillis\":56,\"pauseDurationMillis\":0,\"stages\":[{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/4/execution/node/5/wfapi/describe\"}},\"id\":\"5\",\"name\":\"Build\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416626134,\"durationMillis\":2014,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/4/execution/node/8/wfapi/describe\"}},\"id\":\"8\",\"name\":\"Test\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416628148,\"durationMillis\":2016,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/4/execution/node/11/wfapi/describe\"}},\"id\":\"11\",\"name\":\"Deploy\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416630164,\"durationMillis\":2032,\"pauseDurationMillis\":0}]},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/3/wfapi/describe\"}},\"id\":\"3\",\"name\":\"#3\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416047613,\"endTimeMillis\":1465416053727,\"durationMillis\":6064,\"queueDurationMillis\":50,\"pauseDurationMillis\":0,\"stages\":[{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/3/execution/node/5/wfapi/describe\"}},\"id\":\"5\",\"name\":\"Build\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416047663,\"durationMillis\":2019,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/3/execution/node/8/wfapi/describe\"}},\"id\":\"8\",\"name\":\"Test\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416049682,\"durationMillis\":2018,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/3/execution/node/11/wfapi/describe\"}},\"id\":\"11\",\"name\":\"Deploy\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465416051700,\"durationMillis\":2027,\"pauseDurationMillis\":0}]},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/2/wfapi/describe\"}},\"id\":\"2\",\"name\":\"#2\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415969046,\"endTimeMillis\":1465415975188,\"durationMillis\":6061,\"queueDurationMillis\":81,\"pauseDurationMillis\":0,\"stages\":[{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/2/execution/node/5/wfapi/describe\"}},\"id\":\"5\",\"name\":\"Build\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415969127,\"durationMillis\":2018,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/2/execution/node/8/wfapi/describe\"}},\"id\":\"8\",\"name\":\"Test\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415971145,\"durationMillis\":2017,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/2/execution/node/11/wfapi/describe\"}},\"id\":\"11\",\"name\":\"Deploy\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415973162,\"durationMillis\":2026,\"pauseDurationMillis\":0}]},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/1/wfapi/describe\"}},\"id\":\"1\",\"name\":\"#1\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415926478,\"endTimeMillis\":1465415933015,\"durationMillis\":6086,\"queueDurationMillis\":451,\"pauseDurationMillis\":0,\"stages\":[{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/1/execution/node/5/wfapi/describe\"}},\"id\":\"5\",\"name\":\"Build\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415926929,\"durationMillis\":1996,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/1/execution/node/8/wfapi/describe\"}},\"id\":\"8\",\"name\":\"Test\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415928925,\"durationMillis\":2023,\"pauseDurationMillis\":0},{\"_links\":{\"self\":{\"href\":\"/jenkins/job/Test%20Workflow/1/execution/node/11/wfapi/describe\"}},\"id\":\"11\",\"name\":\"Deploy\",\"execNode\":\"master\",\"status\":\"SUCCESS\",\"startTimeMillis\":1465415930948,\"durationMillis\":2067,\"pauseDurationMillis\":0}]}]";

    private WorkflowApi workflowApi = mock(WorkflowApi.class);

    @Before
    public void setup() throws IOException {
        when(workflowApi.jenkinsUrl()).thenReturn("http://localhost:8080/jenkins/");
        when(workflowApi.lastRunFor(anyString())).thenCallRealMethod();
        when(workflowApi.lastFinishedRunFor(anyString())).thenCallRealMethod();
        when(workflowApi.execute(any(HttpRequest.class))).thenReturn(listOfRunsResponseJson);
    }

    @Test
    public void shouldGetInformationAboutLastRun() {
        when(workflowApi.getRunsFor(anyString())).thenCallRealMethod();

        Run run = workflowApi.lastRunFor("Test Workflow");
        assertNotNull(run);
        assertThat(run.durationMillis, is(6070L));
        assertThat(run.startTimeMillis.getValue(), is(1465416676325L));
        assertThat(run.endTimeMillis.getValue(), is(1465416682442L));
    }

    @Test(expected = IllegalStateException.class)
    public void getRunsForShouldThrowIllegalStateExceptionForIOExceptions() throws IOException {
        when(workflowApi.getRunsFor(anyString())).thenCallRealMethod();
        when(workflowApi.execute(any(HttpRequest.class))).thenThrow(new IOException("Test thrown exception"));
        workflowApi.lastRunFor("Test Workflow");
    }

    @Test
    public void shouldGetLastFinishedRunForJob() {
        when(workflowApi.getRunsFor(anyString())).thenCallRealMethod();

        Run run = workflowApi.lastFinishedRunFor("Test Workflow");
        assertThat(run.id, is("5"));
        assertThat(run.name, is("#5"));
        assertThat(run.status, is("SUCCESS"));
    }

    @Test
    public void shouldNotGetLastFinishedRunForJobIfOnlyInProgressOrPausedJobsExist() {
        Run inProgressRun = new Run(null, null, null, "IN_PROGRESS", null, null, null, null);
        Run pausedRun = new Run(null, null, null, "PAUSED_PENDING_INPUT", null, null, null, null);
        when(workflowApi.getRunsFor(anyString())).thenReturn(Arrays.asList(inProgressRun, pausedRun));

        assertThat(workflowApi.lastFinishedRunFor("Test Workflow"), nullValue());
    }

    @Test
    public void timeoutThresholdShouldAlwaysBePositive() {
        assertThat(WorkflowApi.timeoutThreshold(WorkflowPipelineView.DEFAULT_INTERVAL), greaterThan(0));
        assertThat(WorkflowApi.timeoutThreshold(1), greaterThan(0));
    }

    @Test
    public void timeoutThresholdShouldDefaultIfNegative() {
        assertThat(WorkflowApi.timeoutThreshold(-1000), greaterThan(0));
    }
}
