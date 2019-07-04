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
package se.diabol.jenkins.pipeline.portlet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ReadOnlyDeliveryPipelineViewTest {

    @Test
    public void shouldHaveDefaults() {
        ReadOnlyDeliveryPipelineView view = new ReadOnlyDeliveryPipelineView("name");
        assertEquals(3, view.getNoOfPipelines());
        assertEquals(1, view.getNoOfColumns());
        assertEquals(2, view.getUpdateInterval());
        assertEquals("none", view.getSorting());
        assertNull(view.getEmbeddedCss());
        assertNull(view.getFullScreenCss());
        assertNull(view.getComponentSpecs());
        assertFalse(view.isShowAggregatedPipeline());
        assertFalse(view.getShowAvatars());
        assertFalse(view.isShowChanges());
        assertFalse(view.isAllowManualTriggers());
        assertFalse(view.isShowTotalBuildTime());
        assertFalse(view.isAllowRebuild());
        assertFalse(view.isShowDescription());
        assertFalse(view.isShowPromotions());
        assertFalse(view.isShowTestResults());
        assertFalse(view.isShowStaticAnalysisResults());
        assertFalse(view.getPagingEnabled());
        assertFalse(view.isAllowPipelineStart());
        assertEquals("default", view.getTheme());
        assertNotNull(view.getId());
        assertFalse(view.getIsPortletView());
        assertFalse(view.isShowAggregatedChanges());
        assertNull(view.getAggregatedChangesGroupingPattern());
        assertFalse(view.isEditable());
        assertFalse(view.hasPermission(null));
    }
}
