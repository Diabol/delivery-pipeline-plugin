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
package se.diabol.jenkins.pipeline;

import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PipelinePropertyTest {

    @Test
    public void testDoCheckStageName() {
        PipelineProperty.DescriptorImpl d = new PipelineProperty.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, d.doCheckStageName("").kind);
        assertEquals(FormValidation.Kind.ERROR, d.doCheckStageName(" ").kind);
        assertEquals(FormValidation.Kind.OK, d.doCheckStageName("Stage").kind);
    }

    @Test
    public void testDoCheckTaskName() {
        PipelineProperty.DescriptorImpl d = new PipelineProperty.DescriptorImpl();
        assertEquals(FormValidation.Kind.OK, d.doCheckTaskName("").kind);
        assertEquals(FormValidation.Kind.ERROR, d.doCheckTaskName(" ").kind);
        assertEquals(FormValidation.Kind.OK, d.doCheckTaskName("Task").kind);
    }

    @Test
    public void testIsApplicable() {
        PipelineProperty.DescriptorImpl d = new PipelineProperty.DescriptorImpl();
        assertTrue(d.isApplicable(FreeStyleProject.class));

    }

}
