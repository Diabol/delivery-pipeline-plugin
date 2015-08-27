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
package se.diabol.jenkins.pipeline.token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import hudson.model.FreeStyleProject;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

public class TokenUtilsTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testDecodedTemplate() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);
        assertEquals("1.0.0.1", TokenUtils.decodedTemplate(build, "1.0.0.1"));
    }

    @Test
    public void testDecodedTemplateWithMacroEvaluationException() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);
        assertEquals("", TokenUtils.decodedTemplate(build, "${TEST_NESTEDX}"));
    }

    @Test
    @WithoutJenkins
    public void testDecodedTemplateNoBuild() {
        assertEquals("1.0.0.1", TokenUtils.decodedTemplate(null, "1.0.0.1"));
    }

    @Test
    @WithoutJenkins
    public void testDecodedTemplateWithIOException() throws IOException, InterruptedException {
        final FreeStyleBuild mockFreeStyleBuild = mock(FreeStyleBuild.class);
        when(mockFreeStyleBuild.getEnvironment(TaskListener.NULL)).thenThrow(new IOException());

        assertEquals("", TokenUtils.decodedTemplate(mockFreeStyleBuild, "1.0.0.1"));
    }

    @Test
    @WithoutJenkins
    public void testStringIsNotEmpy() {
        assertEquals(Boolean.TRUE, TokenUtils.stringIsNotEmpty("string"));
        assertEquals(Boolean.FALSE, TokenUtils.stringIsNotEmpty(""));
        assertEquals(Boolean.FALSE, TokenUtils.stringIsNotEmpty(null));
    }

    @Test(expected=IllegalAccessException.class)
    public void testConstructorPrivate() throws Exception {
        TokenUtils.class.newInstance();
        fail("Utility class constructor should be private");
    }
}
