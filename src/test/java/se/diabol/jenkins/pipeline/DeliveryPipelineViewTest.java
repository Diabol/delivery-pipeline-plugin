package se.diabol.jenkins.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeliveryPipelineViewTest {

    @Test
    public void testOnJobRenamed()  {
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<>();
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp1", "build1"));
        componentSpecs.add(new DeliveryPipelineView.ComponentSpec("comp2", "build2"));


        DeliveryPipelineView view = new DeliveryPipelineView("Test", 1, componentSpecs, 1, true);
        view.onJobRenamed(null, "build1", "newbuild");
        assertEquals("newbuild", view.getComponentSpecs().get(0).getFirstJob());

    }


}
