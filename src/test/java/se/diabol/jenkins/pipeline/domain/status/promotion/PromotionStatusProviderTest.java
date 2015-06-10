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
package se.diabol.jenkins.pipeline.domain.status.promotion;

import hudson.model.*;
import hudson.plugins.promoted_builds.Promotion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PromotionStatusProviderTest {

    @Test
    public void testIsBuildPromotedWhenThereAreNoPromotions() {
        final AbstractBuild mockBuild = mock(AbstractBuild.class);
        when(mockBuild.getAction(any(Class.class))).thenReturn(mock(BuildBadgeAction.class));

        final PromotionStatusProvider.PromotedBuildActionWrapper mockPromotedBuildActionWrapper = mock(PromotionStatusProvider.PromotedBuildActionWrapper.class);
        when(mockPromotedBuildActionWrapper.getPromotions(mockBuild)).thenReturn(Collections.emptyList());

        final PromotionStatusProvider promotionStatusProvider = new PromotionStatusProvider();
        promotionStatusProvider.setPromotedBuildActionWrapper(mockPromotedBuildActionWrapper);

        final boolean isPromoted = promotionStatusProvider.isBuildPromoted(mockBuild);
        assertFalse(isPromoted);
    }

    @Test
    public void testIsBuildPromotedWhenThereArePromotions() {
        final AbstractBuild mockBuild = mock(AbstractBuild.class);
        when(mockBuild.getAction(any(Class.class))).thenReturn(mock(BuildBadgeAction.class));

        final List mockPromotionList = new ArrayList();
        mockPromotionList.add(mock(Object.class));

        final PromotionStatusProvider.PromotedBuildActionWrapper mockPromotedBuildActionWrapper = mock(PromotionStatusProvider.PromotedBuildActionWrapper.class);
        when(mockPromotedBuildActionWrapper.getPromotions(anyObject())).thenReturn(mockPromotionList);

        final PromotionStatusProvider promotionStatusProvider = new PromotionStatusProvider();
        promotionStatusProvider.setPromotedBuildActionWrapper(mockPromotedBuildActionWrapper);

        final boolean isPromoted = promotionStatusProvider.isBuildPromoted(mockBuild);
        assertTrue(isPromoted);
    }

    @Test
    public void testGetPromotionStatusList() {
        final String stringParamName = "StringParamName";
        final String stringParamValue = "Param Value";

        final String booleanParamName = "BooleanParamName";
        final boolean booleanParamValue = true;

        final String fileParamName = "FileParamName";
        final String fileParamValue = "File Path";

        final String promotionUserName = "User";
        final String promotionIcon = "16x16-icon";

        final String promotion1Name = "Promotion-1";
        final long promotion1StartTime = 10000L;

        final String promotion2Name = "Promotion-2";
        final long promotion2StartTime = 12000L;

        final List<ParameterValue> mockParameterValues = new ArrayList<ParameterValue>();
        mockParameterValues.add(new StringParameterValue(stringParamName, stringParamValue));
        mockParameterValues.add(new BooleanParameterValue(booleanParamName, booleanParamValue));

        final FileParameterValue mockFileParam = mock(FileParameterValue.class);
        when(mockFileParam.getLocation()).thenReturn(fileParamValue);

        mockParameterValues.add(mockFileParam);

        final Promotion mockPromotionFirst = mock(Promotion.class);
        when(mockPromotionFirst.getUserName()).thenReturn(promotionUserName);
        when(mockPromotionFirst.getParameterValues()).thenReturn(mockParameterValues);

        final Promotion mockPromotionSecond = mock(Promotion.class);
        when(mockPromotionSecond.getParameterValues()).thenReturn(mockParameterValues);

        final List<Promotion> mockPromotions = new ArrayList<Promotion>();
        mockPromotions.add(mockPromotionFirst);

        final PromotionStatusProvider.PromotionStatusWrapper mockPromotionStatusWrapper = mock(PromotionStatusProvider.PromotionStatusWrapper.class);
        when(mockPromotionStatusWrapper.getPromotionBuilds(anyObject())).thenReturn(mockPromotions);

        final List mockStatusList = new ArrayList();
        final Object mockStatusObject1 = new Object();
        mockStatusList.add(mockStatusObject1);

        final Object mockStatusObject2 = new Object();
        mockStatusList.add(mockStatusObject2);

        when(mockPromotionStatusWrapper.getName(mockStatusObject1)).thenReturn(promotion1Name);
        when(mockPromotionStatusWrapper.getIcon(mockStatusObject1, PromotionStatusProvider.DEFAULT_ICON_SIZE)).thenReturn(promotionIcon);
        when(mockPromotionStatusWrapper.getStartTime(mockStatusObject1)).thenReturn(promotion1StartTime);

        when(mockPromotionStatusWrapper.getName(mockStatusObject2)).thenReturn(promotion2Name);
        when(mockPromotionStatusWrapper.getIcon(mockStatusObject2, PromotionStatusProvider.DEFAULT_ICON_SIZE)).thenReturn(promotionIcon);
        when(mockPromotionStatusWrapper.getStartTime(mockStatusObject2)).thenReturn(promotion2StartTime);

        final AbstractBuild mockBuild = mock(AbstractBuild.class);
        when(mockBuild.getAction(any(Class.class))).thenReturn(mock(BuildBadgeAction.class));

        final PromotionStatusProvider.PromotedBuildActionWrapper mockPromotedBuildActionWrapper = mock(PromotionStatusProvider.PromotedBuildActionWrapper.class);
        when(mockPromotedBuildActionWrapper.getPromotions(anyObject())).thenReturn(mockStatusList);

        final PromotionStatusProvider promotionStatusProvider = new PromotionStatusProvider();
        promotionStatusProvider.setPromotedBuildActionWrapper(mockPromotedBuildActionWrapper);
        promotionStatusProvider.setPromotionStatusWrapper(mockPromotionStatusWrapper);

        final List<PromotionStatus> promotionStatusList = promotionStatusProvider.getPromotionStatusList(mockBuild);
        assertNotNull(promotionStatusList);
        assertEquals(2, promotionStatusList.size());

        // list should be in desc sorted by start time
        final PromotionStatus promotionStatus1 = promotionStatusList.get(0);
        assertEquals(promotion1Name, promotionStatus1.getName());
        assertEquals(promotionUserName, promotionStatus1.getUser());
        assertEquals(promotionIcon, promotionStatus1.getIcon());

        final List<String> promotionStatus1Params = promotionStatus1.getParams();
        assertNotNull(promotionStatus1Params);
        assertEquals(3, promotionStatus1Params.size());
        assertEquals("<strong>StringParamName</strong>: Param Value", promotionStatus1Params.get(0));
        assertEquals("<strong>BooleanParamName</strong>: true", promotionStatus1Params.get(1));
        assertEquals("<strong>null</strong>: File Path", promotionStatus1Params.get(2));

        final PromotionStatus promotionStatus2 = promotionStatusList.get(1);
        assertEquals(promotion2Name, promotionStatus2.getName());
        assertEquals(promotionUserName, promotionStatus2.getUser());
        assertEquals(promotionIcon, promotionStatus2.getIcon());

        final List<String> promotionStatus2Params = promotionStatus2.getParams();
        assertNotNull(promotionStatus2Params);
        assertEquals(3, promotionStatus2Params.size());
        assertEquals("<strong>StringParamName</strong>: Param Value", promotionStatus2Params.get(0));
        assertEquals("<strong>BooleanParamName</strong>: true", promotionStatus2Params.get(1));
        assertEquals("<strong>null</strong>: File Path", promotionStatus2Params.get(2));
    }
}
