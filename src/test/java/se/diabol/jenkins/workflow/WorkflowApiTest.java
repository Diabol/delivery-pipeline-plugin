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

import com.google.common.io.Resources;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

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
    public void shouldGetInformationAboutLastRun() {
        // TODO: Implement
    }

    private static String loadResource(String name) {
        URL url = Resources.getResource("wfapi-last_run.json");
        try {
            return Resources.toString(url, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
