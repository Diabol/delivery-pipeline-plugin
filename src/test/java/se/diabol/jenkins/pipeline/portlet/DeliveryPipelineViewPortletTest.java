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
package se.diabol.jenkins.pipeline.portlet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DeliveryPipelineViewPortletTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    private final static String NONE = null;

    @Test
    @WithoutJenkins
    public void testDefaults() throws IOException {
        DeliveryPipelineViewPortlet portlet = new DeliveryPipelineViewPortlet("PipelinePortlet","Build","Test");
        assertNotNull(portlet.getPortletId());
        assertEquals("Build", portlet.getInitialJob());
        assertEquals("Test", portlet.getFinalJob());
        assertEquals("PipelinePortlet", portlet.getName());
    }
}
