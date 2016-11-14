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
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.Pipeline;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PipelinePaginationTest {

    private final static boolean pagingEnabledTrue = true;

    @Test
    public void testPipelinePagination() throws Exception {
    	PipelinePagination pagination = new PipelinePagination(1,50,10,"?page=");
    	assertEquals(1, pagination.getCurrentPage());
    	assertEquals(50, pagination.getTotalCount());
    	assertEquals(10, pagination.getPageSize());
    	assertNotNull(pagination.getTag());
    }
    
    @Test
    public void testPipelinePaginationPrevStep() throws Exception {
    	PipelinePagination pagination = new PipelinePagination(12,50,3,"?page=");
    	assertEquals(12, pagination.getCurrentPage());
    	assertEquals(50, pagination.getTotalCount());
    	assertEquals(3, pagination.getPageSize());
    	assertNotNull(pagination.getTag());
    }
    
    @Test
    public void testPipelinePaginationNextStep() throws Exception {
    	PipelinePagination pagination = new PipelinePagination(1,50,3,"?page=");
    	assertEquals(1, pagination.getCurrentPage());
    	assertEquals(50, pagination.getTotalCount());
    	assertEquals(3, pagination.getPageSize());
    	assertNotNull(pagination.getTag());
    }
    
    @Test
    public void testComponentNumber() {
        Component componentB = new Component("B", "B", "job/A", false, 3, pagingEnabledTrue, 2);
        Component componentA = new Component("A", "A", "job/B", false, 3, pagingEnabledTrue, 1);
        List<Component> list = new ArrayList<Component>();
        list.add(componentA);  
        list.add(componentB);
        assertEquals(1, list.get(0).getComponentNumber());
        assertEquals(2, list.get(1).getComponentNumber());
    }
    
    @Test
    public void testComponent() {
        Component componentA = new Component("A", "A", "job/B", false, 3, pagingEnabledTrue, 1);
        componentA.setPipelines(new ArrayList<Pipeline>());
        assertNotNull(componentA.getPagingData());
        assertNotNull(componentA.getPipelines());
    }
}