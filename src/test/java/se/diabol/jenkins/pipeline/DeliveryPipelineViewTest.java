package se.diabol.jenkins.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeliveryPipelineViewTest {

    @Test
    public void testOnJobRenamed()  {
        List<DeliveryPipelineView.Component> components = new ArrayList<>();
        components.add(new DeliveryPipelineView.Component("comp1", "build1"));
        components.add(new DeliveryPipelineView.Component("comp2", "build2"));


        DeliveryPipelineView view = new DeliveryPipelineView("Test", 1, components, 1, true);
        view.onJobRenamed(null, "build1", "newbuild");
        assertEquals("newbuild", view.getComponents().get(0).getFirstJob());

    }


}
