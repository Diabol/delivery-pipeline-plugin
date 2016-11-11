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

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.status.StatusType;
import se.diabol.jenkins.pipeline.domain.status.promotion.PromotionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static se.diabol.jenkins.pipeline.domain.status.StatusType.FAILED;
import static se.diabol.jenkins.pipeline.domain.status.StatusType.SUCCESS;
import static se.diabol.jenkins.pipeline.test.PipelineUtil.createComponent;

public class FailedJobComparatorTest {


    @Test
    public void shouldSortFailedBeforeSuccessful() {

        Component failedComponent = createComponent(status(FAILED, new DateTime().minusDays(1)));
        Component successfulComponent = createComponent(status(SUCCESS, new DateTime().minusDays(1)));

        List<Component> list = new ArrayList<Component>();
        list.add(successfulComponent);
        list.add(failedComponent);
        Collections.sort(list, new FailedJobComparator.DescriptorImpl().createInstance());
        assertEquals(failedComponent, list.get(0));
        assertEquals(successfulComponent, list.get(1));
    }

    @Test
    public void shouldSortRecentlyRunFirstIfSameStatus() {

        Component failedComponentRunLongAgo = createComponent(status(FAILED, new DateTime().minusDays(10)));
        Component failedComponent = createComponent(status(FAILED, new DateTime().minusDays(1)));
        Component successfulComponent = createComponent(status(SUCCESS, new DateTime().minusDays(1)));
        List<Component> list = new ArrayList<Component>();
        list.add(successfulComponent);
        list.add(failedComponent);
        list.add(failedComponentRunLongAgo);
        Collections.sort(list, new FailedJobComparator.DescriptorImpl().createInstance());
        assertEquals(failedComponent, list.get(0));
        assertEquals(failedComponentRunLongAgo, list.get(1));
        assertEquals(successfulComponent, list.get(2));
    }

    private Status status(StatusType statusType, DateTime lastRunAt) {
        return new SimpleStatus(statusType, lastRunAt.getMillis(), 10, false, Lists.<PromotionStatus>newArrayList());
    }

}
