package se.diabol.jenkins.pipeline.util;

import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import se.diabol.jenkins.pipeline.PipelineProperty;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjectUtilTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();


    @Test
    public void testGetStageNames() throws Exception {
        FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");

        Set<String> stageNames = ProjectUtil.getStageNames();
        assertNotNull(stageNames);
        assertEquals(0, stageNames.size());
        build1.addProperty(new PipelineProperty(null, "Build"));
        build2.addProperty(new PipelineProperty(null, "QA"));

        stageNames = ProjectUtil.getStageNames();
        assertNotNull(stageNames);
        assertEquals(2, stageNames.size());
        assertTrue(stageNames.contains("Build"));
        assertTrue(stageNames.contains("QA"));
    }

}
