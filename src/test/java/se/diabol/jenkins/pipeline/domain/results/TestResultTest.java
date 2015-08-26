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
package se.diabol.jenkins.pipeline.domain.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;

import java.util.List;

import org.junit.Test;

public class TestResultTest {

    @Test
    public void testGetTestResult() {
        AbstractBuild<?, ?> build =  mock(AbstractBuild.class);
        AggregatedTestResultAction tests = mock(AggregatedTestResultAction.class);
        when(build.getAction(AggregatedTestResultAction.class)).thenReturn(tests);
        when(tests.getDisplayName()).thenReturn("Test Result");
        when(tests.getFailCount()).thenReturn(1);
        when(tests.getSkipCount()).thenReturn(0);
        when(tests.getTotalCount()).thenReturn(11);

        List<TestResult> result = TestResult.getResults(build);
        assertNotNull(result);
        assertEquals(1, result.get(0).getFailed());
        assertEquals(0, result.get(0).getSkipped());
        assertEquals(11, result.get(0).getTotal());
        assertNotNull(result.get(0).getUrl());
        assertNotNull(result.get(0).getName());
    }

    @Test
    public void testGetTestResultFreeStyleBuild() {
        FreeStyleBuild build =  mock(FreeStyleBuild.class);
        TestResultAction action = mock(TestResultAction.class);
        when(build.getAction(TestResultAction.class)).thenReturn(action);

        List<TestResult> result = TestResult.getResults(build);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetTestResultBuildNull() {
        List<TestResult> result = TestResult.getResults(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

}
