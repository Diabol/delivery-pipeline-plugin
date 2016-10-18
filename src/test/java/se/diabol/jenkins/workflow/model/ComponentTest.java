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

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Test;
import org.kohsuke.stapler.export.Exported;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ComponentTest {

    @Test
    public void shouldBeConsideredWorkflowComponent() {
        assertTrue(new Component(null, null, null).isWorkflowComponent());
    }

    @Test
    public void shouldProperlyFormatExpectedWorkflowUrl() {
        final String expectedName = "JobName";
        WorkflowJob workflowJob = new WorkflowJob(null, expectedName);
        Component component = new Component("Workflow component", workflowJob, null);
        assertThat(component.getWorkflowUrl(), is("job/" + expectedName + "/"));
    }

    @Test
    public void shouldExposeWorkflowJob() {
        final WorkflowJob workflowJob = new WorkflowJob(null, "Name");
        Component component = new Component("Component", workflowJob, null);
        assertThat(component.getWorkflowJob(), is(workflowJob));
    }

    @Test
    public void shouldExposePipelines() {
        final List<Pipeline> pipelines = new ArrayList<Pipeline>();
        Component component = new Component("Component", null, pipelines);
        assertThat(component.getPipelines(), is(pipelines));
    }

    @Test
    public void shouldHaveProperToString() {
        final String componentName = "Component Name";
        Component component = new Component(componentName, null, Collections.<Pipeline>emptyList());
        final String toString = component.toString();
        assertTrue(toString.contains(componentName));
        assertTrue(toString.contains("pipelines"));
    }

    @Test
    public void shouldHaveExportedProperties() throws NoSuchMethodException {
        assertTrue(methodHasExportedAnnoation("isWorkflowComponent"));
        assertTrue(methodHasExportedAnnoation("getWorkflowUrl"));
        assertTrue(methodHasExportedAnnoation("getWorkflowJob"));
        assertTrue(methodHasExportedAnnoation("getPipelines"));
    }

    private boolean methodHasExportedAnnoation(String methodName) throws NoSuchMethodException {
        Class<Component> componentClass = Component.class;
        for (Annotation annotation : componentClass.getMethod(methodName).getAnnotations()) {
            if (annotation.annotationType().equals(Exported.class)) {
                return true;
            }
        }
        return false;
    }
}
