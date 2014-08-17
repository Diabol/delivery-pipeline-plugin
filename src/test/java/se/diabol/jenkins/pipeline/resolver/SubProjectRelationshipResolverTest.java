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
package se.diabol.jenkins.pipeline.resolver;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SubProjectRelationshipResolverTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testExtractPipelineWithSubProjects() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");

        build.getBuildersList().add(new TriggerBuilder(new BlockableBuildTriggerConfig("sonar", new BlockingBehaviour("never", "never", "never"), null)));

        jenkins.getInstance().rebuildDependencyGraph();
        SubProjectRelationshipResolver resolver = new SubProjectRelationshipResolver();
        List<AbstractProject> downStreams = resolver.getDownstreamProjects(build);
        assertEquals(1, downStreams.size());
        assertEquals(sonar, downStreams.get(0));


    }

}
