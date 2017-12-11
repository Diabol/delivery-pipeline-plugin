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

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BuildPipelineRelationshipResolverTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testExtractPipelineWithBuildPipelineTrigger() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject deploy01 = jenkins.createFreeStyleProject("deploy_step01");
        FreeStyleProject deploy02 = jenkins.createFreeStyleProject("deploy_step02");

        build.getPublishersList().add(new BuildPipelineTrigger("deploy_step02, deploy_step01", null));
        jenkins.getInstance().rebuildDependencyGraph();

        BuildPipelineRelationshipResolver resolver = new BuildPipelineRelationshipResolver();
        List<AbstractProject> downstreams = resolver.getDownstreamProjects(build);
        assertEquals(2, downstreams.size());
        assertEquals(deploy01, downstreams.get(1));
        assertEquals(deploy02, downstreams.get(0));

    }
}
