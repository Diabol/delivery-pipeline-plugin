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

import com.google.common.collect.Lists;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class EnvVersionTokenMacroTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testNoParseVersionExists() throws Exception {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(projectA);

        String string =
                macro.evaluate(build, new StreamTaskListener(System.err, Charset.defaultCharset()), "ENV_VERSION");
        assertEquals("", string);
    }

    @Test
    public void testParseVersionExists() throws Exception {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        TaskListener listener = new StreamTaskListener(System.err, Charset.defaultCharset());
        FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(projectA);

        //Due to https://issues.jenkins-ci.org/browse/SECURITY-170
        ParametersAction action = new ParametersAction(Lists.newArrayList(new StringParameterValue("ENV_VERSION",
                "1.0-SNAPSHOT")), Lists.newArrayList("ENV_VERSION"));
        build.addAction(action);
        String string = macro.evaluate(build, listener, "ENV_VERSION");
        assertEquals("1.0-SNAPSHOT", string);
    }

    @Test
    public void testParseWithStripedVersionExists() throws Exception {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        final TaskListener listener = new StreamTaskListener(System.err, Charset.defaultCharset());
        FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        jenkins.setQuietPeriod(0);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(projectA);

        //Due to https://issues.jenkins-ci.org/browse/SECURITY-170
        ParametersAction action = new ParametersAction(Lists.newArrayList(new StringParameterValue("ENV_VERSION",
                "1.0-SNAPSHOT")), Lists.newArrayList("ENV_VERSION"));
        build.addAction(action);

        macro.stripSnapshot = true;
        String string = macro.evaluate(build, listener, "ENV_VERSION");
        assertEquals("1.0", string);
    }

    @Test
    @WithoutJenkins
    public void testAcceptsMacroName() {
        EnvVersionTokenMacro macro = new EnvVersionTokenMacro();
        assertTrue(macro.acceptsMacroName("ENV_VERSION"));
    }

}
