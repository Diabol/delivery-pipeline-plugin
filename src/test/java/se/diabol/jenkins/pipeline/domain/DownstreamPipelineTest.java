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
package se.diabol.jenkins.pipeline.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownstreamPipelineTest {

    private static final boolean PAGING_ENABLED = true;
    private static final boolean PAGING_DISABLED = false;

    @Test
    public void getStartIndexShouldReturnZeroWhenPagingDisabled() throws Exception {
        Component component = mock(Component.class);
        when(component.isFullScreenView()).thenReturn(true);
        int startIndex = DownstreamPipeline.getStartIndex(component, PAGING_DISABLED, 10);
        assertThat(startIndex, is(0));

        when(component.isFullScreenView()).thenReturn(false);
        startIndex = DownstreamPipeline.getStartIndex(component, PAGING_DISABLED, 10);
        assertThat(startIndex, is(0));
    }

    @Test
    public void getStartIndexShouldReturnZeroWhenPagingEnabledAndInFullScreen() throws Exception {
        Component component = mock(Component.class);
        when(component.isFullScreenView()).thenReturn(true);
        int startIndex = DownstreamPipeline.getStartIndex(component, PAGING_ENABLED, 10);
        assertThat(startIndex, is(0));
    }

    @Test
    public void shouldCalculateStartIndexWhenPagingEnabledAndInNormalView() throws Exception {
        Component component = mock(Component.class);
        when(component.isFullScreenView()).thenReturn(false);
        when(component.getCurrentPage()).thenReturn(3);
        int startIndex = DownstreamPipeline.getStartIndex(component, PAGING_ENABLED, 10);
        assertThat(startIndex, is(20));
    }

    @Test
    public void calculateRetrieveSizeShouldReturnNoOfPipelinesWhenPagingDisabled() {
        Component component = mock(Component.class);
        when(component.isFullScreenView()).thenReturn(false);
        final int numberOfPipelines = 9;
        int retrieveSize = DownstreamPipeline.calculateRetreiveSize(component, PAGING_DISABLED, numberOfPipelines, 10);
        assertThat(retrieveSize, is(numberOfPipelines));
    }

    @Test
    public void calculateRetrieveSizeShouldReturnNoOfPipelinesWhenPagingEnabledAndInFullScreen() {
        Component component = mock(Component.class);
        when(component.isFullScreenView()).thenReturn(true);
        final int numberOfPipelines = 9;
        int retrieveSize = DownstreamPipeline.calculateRetreiveSize(component, PAGING_ENABLED, numberOfPipelines, 10);
        assertThat(retrieveSize, is(numberOfPipelines));
    }

    @Test
    public void shouldCalculateRetrieveSizeWhenPagingEnabledAndInNormalView() {
        Component component = mock(Component.class);
        when(component.isFullScreenView()).thenReturn(false);
        when(component.getCurrentPage()).thenReturn(0);
        int numberOfPipelines = 10;
        int retrieveSize = DownstreamPipeline.calculateRetreiveSize(component, PAGING_ENABLED, numberOfPipelines, 5);
        assertThat(retrieveSize, is(numberOfPipelines));

        numberOfPipelines = 5;
        retrieveSize = DownstreamPipeline.calculateRetreiveSize(component, PAGING_ENABLED, numberOfPipelines, 10);
        assertThat(retrieveSize, is(numberOfPipelines));

        when(component.getCurrentPage()).thenReturn(1);
        retrieveSize = DownstreamPipeline.calculateRetreiveSize(component, PAGING_ENABLED, numberOfPipelines, 10);
        assertThat(retrieveSize, is(5));
    }

    @Test
    public void classShouldIndicateToAlwaysShowUpstreamJobs() {
        assertTrue(new DownstreamPipeline(null, null, null, null).showUpstream());
    }

}
