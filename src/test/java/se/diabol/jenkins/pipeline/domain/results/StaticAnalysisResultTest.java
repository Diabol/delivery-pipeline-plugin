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
import hudson.Plugin;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.BuildResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class)
public class StaticAnalysisResultTest {

    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    @Test
    public void testGetStaticAnalysisResult() {
        AbstractBuild<?, ?> build =  mock(AbstractBuild.class);
        Jenkins jenkins = mock(Jenkins.class);
        Plugin plugin = mock(Plugin.class);
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getPlugin("analysis-core")).thenReturn(plugin);
        AbstractResultAction findbugs = mock(AbstractResultAction.class);
        AbstractResultAction owasp = mock(AbstractResultAction.class);

        when(findbugs.getDisplayName()).thenReturn("FindBugs Warnings");
        when(owasp.getDisplayName()).thenReturn("Dependency-Check Warnings");

        BuildResult r1 = mock(BuildResult.class);
        when(r1.getNumberOfHighPriorityWarnings()).thenReturn(1);
        when(r1.getNumberOfNormalPriorityWarnings()).thenReturn(2);
        when(r1.getNumberOfLowPriorityWarnings()).thenReturn(3);

        BuildResult r2 = mock(BuildResult.class);
        when(r2.getNumberOfHighPriorityWarnings()).thenReturn(1);
        when(r2.getNumberOfNormalPriorityWarnings()).thenReturn(2);
        when(r2.getNumberOfLowPriorityWarnings()).thenReturn(3);

        List<Action> actions = new ArrayList<Action>();
        actions.add(findbugs);
        actions.add(owasp);

        findbugs.setResult(r1);
        owasp.setResult(r2);

        when(build.getActions()).thenReturn(actions);

        List<StaticAnalysisResult> result = StaticAnalysisResult.getResults(build);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("FindBugs Warnings", result.get(0).getName());
        assertEquals(1, result.get(0).getHigh());
        assertEquals(2, result.get(0).getNormal());
        assertEquals(3, result.get(0).getLow());
        System.out.println(result.get(0));
        assertNotNull(result.get(0).getUrl());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetStaticAnalysisResultEmpty() {
        AbstractBuild<?, ?> build =  mock(AbstractBuild.class);
        Jenkins jenkins = mock(Jenkins.class);
        Plugin plugin = mock(Plugin.class);
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getPlugin("analysis-core")).thenReturn(plugin);
        when(build.getActions()).thenReturn(Collections.<Action> emptyList());

        List<StaticAnalysisResult> result = StaticAnalysisResult.getResults(build);
        assertEquals(Collections.<Action> emptyList(), result);
    }

    @Test
    public void testGetStaticAnalysisResultNull() {
        List<StaticAnalysisResult> result = StaticAnalysisResult.getResults(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

}
