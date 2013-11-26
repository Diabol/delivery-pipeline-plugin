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
package se.diabol.jenkins.pipeline.util;

import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.mortbay.jetty.security.UserRealm;
import se.diabol.jenkins.pipeline.PipelineProperty;

import java.util.Set;

import static org.junit.Assert.*;

public class ProjectUtilTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();



    @Test
    public void testFillAllProjects() throws Exception {
        FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        ListBoxModel list = ProjectUtil.fillAllProjects(jenkins.getInstance());
        assertEquals(2, list.size());
        ListBoxModel.Option option1 = list.get(0);
        assertEquals(build1.getDisplayName(), option1.name);


        ListBoxModel.Option option2 = list.get(1);
        assertEquals(build2.getDisplayName(), option2.name);


    }

}
