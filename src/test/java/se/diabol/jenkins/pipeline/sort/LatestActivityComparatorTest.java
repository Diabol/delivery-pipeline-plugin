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

import org.joda.time.DateTime;
import org.junit.Test;
import se.diabol.jenkins.pipeline.domain.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static se.diabol.jenkins.pipeline.domain.status.StatusType.SUCCESS;
import static se.diabol.jenkins.pipeline.test.PipelineUtil.createComponent;
import static se.diabol.jenkins.pipeline.test.PipelineUtil.status;

public class LatestActivityComparatorTest {


    @Test
    public void shouldSortRecentlyRunnedPipelinesFirst() {
        Component componentRunLongAgo = createComponent(status(SUCCESS, new DateTime().minusDays(2)));
        Component componentRunRecently = createComponent(status(SUCCESS, new DateTime().minusDays(1)));
        List<Component> list = new ArrayList<Component>();
        list.add(componentRunRecently);
        list.add(componentRunLongAgo);
        Collections.sort(list, new LatestActivityComparator.DescriptorImpl().createInstance());
        assertEquals(componentRunRecently, list.get(0));
        assertEquals(componentRunLongAgo, list.get(1));
    }
}
