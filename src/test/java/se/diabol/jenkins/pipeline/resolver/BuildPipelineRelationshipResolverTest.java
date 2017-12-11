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
