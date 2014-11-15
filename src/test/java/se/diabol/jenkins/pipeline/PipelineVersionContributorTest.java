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

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.cli.BuildCommand;
import hudson.model.*;
import hudson.tasks.BuildTrigger;
import hudson.util.StreamTaskListener;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

import static org.junit.Assert.*;

public class PipelineVersionContributorTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();


    @Test
    public void testVersionContributorNotConfigured() throws Exception {

        FreeStyleProject firstProject = jenkins.createFreeStyleProject("firstProject");
        FreeStyleProject secondProject = jenkins.createFreeStyleProject("secondProject");
        firstProject.getPublishersList().add(new BuildTrigger("secondProject", false));
        firstProject.save();

        firstProject.getBuildersList().add(new AssertNoPipelineVersion());
        secondProject.getBuildersList().add(new AssertNoPipelineVersion());

        jenkins.setQuietPeriod(0);
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(firstProject);
        jenkins.waitUntilNoActivity();

        assertNotNull(firstProject.getLastBuild());
        assertNotNull(secondProject.getLastBuild());

    }

    @Test
    public void testVersionNoDisplayname() throws Exception {
        FreeStyleProject firstProject = jenkins.createFreeStyleProject("firstProject");
        FreeStyleProject secondProject = jenkins.createFreeStyleProject("secondProject");
        firstProject.getPublishersList().add(new BuildTrigger("secondProject", false));

        firstProject.getBuildWrappersList().add(new PipelineVersionContributor(false, "1.0.0.${BUILD_NUMBER}"));

        firstProject.getBuildersList().add(new AssertPipelineVersion("1.0.0.1"));
        secondProject.getBuildersList().add(new AssertPipelineVersion("1.0.0.1"));

        jenkins.setQuietPeriod(0);
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(firstProject);
        jenkins.waitUntilNoActivity();

        assertNotNull(firstProject.getLastBuild());
        assertNotNull(secondProject.getLastBuild());
        assertEquals("#1", firstProject.getLastBuild().getDisplayName());


    }

    @Test
    public void testVersionContributorConfigured() throws Exception {

        FreeStyleProject firstProject = jenkins.createFreeStyleProject("firstProject");
        FreeStyleProject secondProject = jenkins.createFreeStyleProject("secondProject");
        firstProject.getPublishersList().add(new BuildTrigger("secondProject", false));

        firstProject.getBuildWrappersList().add(new PipelineVersionContributor(true, "1.0.0.${BUILD_NUMBER}"));

        firstProject.getBuildersList().add(new AssertPipelineVersion("1.0.0.1"));
        secondProject.getBuildersList().add(new AssertPipelineVersion("1.0.0.1"));

        jenkins.setQuietPeriod(0);
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(firstProject);
        jenkins.waitUntilNoActivity();

        assertNotNull(firstProject.getLastBuild());
        assertNotNull(secondProject.getLastBuild());
        assertEquals("1.0.0.1", firstProject.getLastBuild().getDisplayName());


    }

    @Test
    public void testVersionContributorConfiguredManualTrigger() throws Exception {

        FreeStyleProject firstProject = jenkins.createFreeStyleProject("firstProject");
        FreeStyleProject secondProject = jenkins.createFreeStyleProject("secondProject");
        firstProject.getPublishersList().add(new BuildPipelineTrigger("secondProject", null));
        firstProject.save();

        firstProject.getBuildWrappersList().add(new PipelineVersionContributor(true, "1.0.0.${BUILD_NUMBER}"));

        firstProject.getBuildersList().add(new AssertPipelineVersion("1.0.0.1"));
        secondProject.getBuildersList().add(new AssertNoPipelineVersion());

        jenkins.setQuietPeriod(0);
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(firstProject);
        jenkins.waitUntilNoActivity();

        assertNotNull(firstProject.getLastBuild());
        assertNull(secondProject.getLastBuild());
        assertEquals("1.0.0.1", firstProject.getLastBuild().getDisplayName());

        secondProject.getBuildersList().clear();
        secondProject.getBuildersList().add(new AssertPipelineVersion("1.0.0.1"));

        BuildPipelineView view = new BuildPipelineView("", "", new DownstreamProjectGridBuilder("firstProject"), "1", false, null);
        view.triggerManualBuild(1, "secondProject", "firstProject");
        jenkins.waitUntilNoActivity();

        assertNotNull(secondProject.getLastBuild());

    }

    @Test
    public void testIsApplicable() throws Exception {
        PipelineVersionContributor.DescriptorImpl d = new PipelineVersionContributor.DescriptorImpl();
        assertTrue(d.isApplicable(jenkins.createFreeStyleProject("a")));

    }

    @Test
    @Bug(21070)
    public void testVersionContributorErrorInPattern() throws Exception {

        FreeStyleProject project = jenkins.createFreeStyleProject("firstProject");

        project.getBuildWrappersList().add(new PipelineVersionContributor(true, "${GFGFGFG}"));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        assertNotNull(build);

        assertEquals("#1", project.getLastBuild().getDisplayName());
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains("Error creating version"));
    }


    @Test
    public void testGetVersionFound() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("firstProject");
        FreeStyleBuild build = project.scheduleBuild2(0, new BuildCommand.CLICause(), new ParametersAction(new StringParameterValue("HEPP", "HOPP"), new StringParameterValue("PIPELINE_VERSION", "1.1"))).get();
        assertEquals("1.1", PipelineVersionContributor.getVersion(build));
    }

    @Test
    public void testGetVersionNotFound() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("firstProject");
        FreeStyleBuild build = project.scheduleBuild2(0, new BuildCommand.CLICause(), new ParametersAction(new StringParameterValue("HEPP", "HOPP"))).get();
        assertNull(PipelineVersionContributor.getVersion(build));
    }


    private class AssertNoPipelineVersion extends TestBuilder {
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                               BuildListener listener) throws InterruptedException, IOException {
            EnvVars env = build.getEnvironment(new StreamTaskListener(System.out, null));
            assertFalse(env.containsKey("PIPELINE_VERSION"));
            return true;
        }
    }

    private class AssertPipelineVersion extends TestBuilder {
        private String version;

        private AssertPipelineVersion(String version) {
            this.version = version;
        }

        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                               BuildListener listener) throws InterruptedException, IOException {
            EnvVars env = build.getEnvironment(new StreamTaskListener(System.out, null));
            assertTrue(env.containsKey("PIPELINE_VERSION"));
            assertEquals(version, env.get("PIPELINE_VERSION"));
            return true;
        }
    }

}
