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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeliveryPipelineViewTest {

    @Test
    public void testOnJobRenamed() {
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1"));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2"));


        DeliveryPipelineView view = new DeliveryPipelineView("Test", componentSpecs);
        view.onJobRenamed(null, "build1", "newbuild");
        assertEquals("newbuild", view.getComponentSpecs().get(0).getFirstJob());
    }

    @Test
    public void testOnJobRenamedDelete() {
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1"));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2"));


        DeliveryPipelineView view = new DeliveryPipelineView("Test", componentSpecs);
        assertEquals(2, view.getComponentSpecs().size());
        view.onJobRenamed(null, "build1", null);

        assertEquals(1, view.getComponentSpecs().size());

    }

}
