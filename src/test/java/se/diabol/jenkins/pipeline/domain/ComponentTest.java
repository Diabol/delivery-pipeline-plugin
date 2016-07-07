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
package se.diabol.jenkins.pipeline.domain;

import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.mockito.runners.MockitoJUnitRunner;
import se.diabol.jenkins.pipeline.DeliveryPipelineView;
import se.diabol.jenkins.pipeline.PipelineProperty;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class ComponentTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    private final static boolean pagingEnabledFalse = false;

    @Test
    @WithoutJenkins
    public void testSettersAndGetters() {
        Component component = new Component("Component", "Build", null, false, 10, pagingEnabledFalse, 1);
        component.setTotalNoOfPipelines(10);
        component.setPipelines(new ArrayList<Pipeline>());
        assertEquals(1, component.getComponentNumber());
        assertEquals("Component", component.getName());
        assertEquals("Build", component.getFirstJob());
        assertFalse(component.isFirstJobParameterized());
        assertNull(component.getFirstJobUrl());
        assertEquals(0, component.getPipelines().size());
        assertNotNull(component.getPipelines());
    }

    @Test
    public void testComponentPaging() throws Exception {
        FreeStyleProject compile = jenkins.createFreeStyleProject("comp");
        FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");
        FreeStyleProject test = jenkins.createFreeStyleProject("test");

        compile.addProperty(new PipelineProperty("Compile", "Build", ""));
        compile.save();

        deploy.addProperty(new PipelineProperty("Deploy", "Deploy", ""));
        deploy.save();
        test.addProperty(new PipelineProperty("Test", "Test", ""));
        test.save();

        compile.getPublishersList().add(new BuildTrigger("test", false));
        test.getPublishersList().add(new BuildTrigger("deploy", false));

        jenkins.getInstance().rebuildDependencyGraph();
        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        view.setPagingEnabled(true);

        DeliveryPipelineView.ComponentSpec componentSpec = new DeliveryPipelineView.ComponentSpec("Pipeline","comp", null);
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        componentSpecs.add(componentSpec);
        view.setComponentSpecs(componentSpecs);

        jenkins.getInstance().addView(view);

        jenkins.setQuietPeriod(0);
        for(int loopIndex=0; loopIndex < 5; loopIndex++) {
            jenkins.buildAndAssertSuccess(compile);
            jenkins.waitUntilNoActivity();
        }

        assertNotNull(view);
        assertEquals(1, view.getPipelines().size());
        assertEquals("Pipeline", view.getViewName());
        assertTrue(view.getPagingEnabled());
        Component component = view.getPipelines().get(0);
        assertEquals(1, component.getCurrentPage());
        assertEquals(3, component.getPipelines().size());
        assertEquals(5, component.getTotalNoOfPipelines());
        assertNotNull(component.getPagingData());
    }
}
