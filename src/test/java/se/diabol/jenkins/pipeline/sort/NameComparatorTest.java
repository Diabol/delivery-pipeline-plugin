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
package se.diabol.jenkins.pipeline.sort;

import org.junit.Test;
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.Pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class NameComparatorTest {

    @Test
    public void testCompare() {
        Component componentB = new Component("B", "B", "job/A", false, new ArrayList<Pipeline>());
        Component componentA = new Component("A", "A", "job/B", false, new ArrayList<Pipeline>());
        List<Component> list = new ArrayList<Component>();
        list.add(componentB);
        list.add(componentA);
        Collections.sort(list, new NameComparator.DescriptorImpl().createInstance());
        assertEquals(componentA, list.get(0));
        assertEquals(componentB, list.get(1));
    }

}
