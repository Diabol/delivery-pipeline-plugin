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
package se.diabol.jenkins.pipeline;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import hudson.util.StreamTaskListener;

import org.jenkinsci.plugins.buildnamesetter.BuildNameSetter;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import java.nio.charset.Charset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PipelineVersionTokenMacroTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testWithBuildNameSetterPlugin() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        FreeStyleProject b = jenkins.createFreeStyleProject("b");

        a.getPublishersList().add(new BuildTrigger("b", false));
        a.getBuildWrappersList().add(new PipelineVersionContributor(true, "1.0.0.$BUILD_NUMBER"));
        b.getBuildWrappersList().add(new BuildNameSetter("$PIPELINE_VERSION"));


        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        jenkins.buildAndAssertSuccess(a);
        jenkins.waitUntilNoActivity();

        assertEquals("1.0.0.1", a.getLastBuild().getDisplayName());
        assertEquals("1.0.0.1", b.getLastBuild().getDisplayName());

    }

    @Test(expected=MacroEvaluationException.class)
    public void testNoPipelineVersionExists() throws Exception {
        PipelineVersionTokenMacro macro = new PipelineVersionTokenMacro();
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);

        macro.evaluate(build, new StreamTaskListener(System.err, Charset.defaultCharset()), "PIPELINE_VERSION");
        fail("Should throw exception");
    }

    @Test
    @WithoutJenkins
    public void testAcceptsMacroName() {
        PipelineVersionTokenMacro macro = new PipelineVersionTokenMacro();
        assertTrue(macro.acceptsMacroName("PIPELINE_VERSION"));
        assertFalse(macro.acceptsMacroName("pipeline_version"));
        assertFalse(macro.acceptsMacroName("BANANA"));
    }

}
