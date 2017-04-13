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
import hudson.model.Result;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.conditions.DownstreamPassCondition;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PromotedBuildRelationshipResolverTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testResolveWithSimpleTrigger() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.createFreeStyleProject("b");
        jenkins.createFreeStyleProject("c");
        a.getPublishersList().add(new BuildTrigger("b", false));
        JobPropertyImpl property = new JobPropertyImpl(a);
        PromotionProcess process = property.addProcess("process");
        process.conditions.add(new DownstreamPassCondition("b", false));
        process.getBuildSteps().add(new BuildTrigger("c", false));
        process.save();
        a.addProperty(property);

        jenkins.getInstance().rebuildDependencyGraph();
        PromotedBuildRelationshipResolver resolver = new PromotedBuildRelationshipResolver();
        List<AbstractProject> projects = resolver.getDownstreamProjects(a);
        assertEquals(1, projects.size());
    }

    @Test
    public void testResolveWithParamTrigger() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.createFreeStyleProject("b");
        jenkins.createFreeStyleProject("c");
        a.getPublishersList().add(new BuildTrigger("b", false));
        JobPropertyImpl property = new JobPropertyImpl(a);
        PromotionProcess process = property.addProcess("process");
        process.conditions.add(new DownstreamPassCondition("b", false));
        process.getBuildSteps().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(new BuildTriggerConfig("c", ResultCondition.ALWAYS)));
        process.save();
        a.addProperty(property);

        jenkins.getInstance().rebuildDependencyGraph();
        PromotedBuildRelationshipResolver resolver = new PromotedBuildRelationshipResolver();
        List<AbstractProject> projects = resolver.getDownstreamProjects(a);
        assertEquals(1, projects.size());
    }

    @Test
    @Issue("JENKINS-28347")
    public void testResolveWithBuildTrigger() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("a");
        jenkins.createFreeStyleProject("b");
        jenkins.createFreeStyleProject("c");
        a.getPublishersList().add(new BuildTrigger("b", false));
        JobPropertyImpl property = new JobPropertyImpl(a);
        PromotionProcess process = property.addProcess("process");
        process.conditions.add(new DownstreamPassCondition("b", false));
        process.getBuildSteps().add(new hudson.plugins.parameterizedtrigger.TriggerBuilder(new BlockableBuildTriggerConfig("b", new BlockingBehaviour(Result.FAILURE, Result.UNSTABLE, Result.FAILURE), null)));
        process.save();
        a.addProperty(property);

        jenkins.getInstance().rebuildDependencyGraph();
        PromotedBuildRelationshipResolver resolver = new PromotedBuildRelationshipResolver();
        List<AbstractProject> projects = resolver.getDownstreamProjects(a);
        assertEquals(1, projects.size());
    }
}
