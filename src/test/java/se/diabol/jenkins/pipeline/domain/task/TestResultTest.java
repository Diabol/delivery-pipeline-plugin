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
package se.diabol.jenkins.pipeline.domain.task;

import hudson.model.AbstractBuild;
import hudson.tasks.test.AggregatedTestResultAction;

import org.junit.Test;

import se.diabol.jenkins.pipeline.domain.task.TestResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestResultTest {

    @Test
    public void testGetTestResult() {
        AbstractBuild build =  mock(AbstractBuild.class);
        AggregatedTestResultAction tests = mock(AggregatedTestResultAction.class);
        when(build.getAction(AggregatedTestResultAction.class)).thenReturn(tests);
        when(tests.getFailCount()).thenReturn(1);
        when(tests.getSkipCount()).thenReturn(0);
        when(tests.getTotalCount()).thenReturn(11);

        TestResult result = TestResult.getTestResult(build);
        assertNotNull(result);
        assertEquals(1, result.getFailed());
        assertEquals(0, result.getSkipped());
        assertEquals(11, result.getTotal());
        assertNotNull(result.getUrl());
    }


    @Test
    public void testGetTestResultEmpty() {
        AbstractBuild build =  mock(AbstractBuild.class);
        when(build.getAction(AggregatedTestResultAction.class)).thenReturn(null);

        TestResult result = TestResult.getTestResult(build);
        assertNull(result);
    }

    @Test
    public void testGetTestResultBuildNull() {
        TestResult result = TestResult.getTestResult(null);
        assertNull(result);
    }


}
