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

import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowApiTest {

    private Jenkins jenkins = mock(Jenkins.class);

    @Before
    public void setup() {
        when(jenkins.getRootUrl()).thenReturn("http://localhost:8080/jenkins");
    }

    @Test
    @Ignore // TODO: Implement
    public void shouldGetInformationAboutLastRun() {
        WorkflowApi workflowApi = new WorkflowApi(jenkins);
        workflowApi.lastRunFor("Test Workflow");
    }
}
