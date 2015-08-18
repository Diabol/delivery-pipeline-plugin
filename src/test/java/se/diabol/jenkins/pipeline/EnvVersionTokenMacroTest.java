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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.util.StreamTaskListener;

import java.nio.charset.Charset;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

public class EnvVersionTokenMacroTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testNoParseVersionExists() throws Exception {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);

        String o = macro.evaluate(build, new StreamTaskListener(System.err, Charset.defaultCharset()), "ENV_VERSION");
        assertEquals("", o);
    }

    @Test
    public void testParseVersionExists() throws Exception {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        TaskListener listener = new StreamTaskListener(System.err, Charset.defaultCharset());
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);

        ParametersAction action = new ParametersAction(new StringParameterValue("ENV_VERSION", "1.0-SNAPSHOT"));
        build.addAction(action);
        String o = macro.evaluate(build, listener, "ENV_VERSION");
        assertEquals("1.0-SNAPSHOT", o);
    }

    @Test
    public void testParseWithStripedVersionExists() throws Exception {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        TaskListener listener = new StreamTaskListener(System.err, Charset.defaultCharset());
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(a);

        ParametersAction action = new ParametersAction(new StringParameterValue("ENV_VERSION", "1.0-SNAPSHOT"));
        build.addAction(action);

        macro.stripSnapshot = true;
        String o = macro.evaluate(build, listener, "ENV_VERSION");
        assertEquals("1.0", o);
    }

    @Test
    @WithoutJenkins
    public void testAcceptsMacroName() {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        assertTrue(macro.acceptsMacroName("ENV_VERSION"));
    }

}
