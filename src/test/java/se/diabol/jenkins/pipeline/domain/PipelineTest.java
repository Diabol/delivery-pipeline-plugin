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
package se.diabol.jenkins.pipeline.domain;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.StandardBuildCard;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.Cause;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.Saveable;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import join.JoinTrigger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.task.Task;
import se.diabol.jenkins.pipeline.util.BuildUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelineTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    private static final boolean PAGING_DISABLED = false;
    private static final boolean SHOW_CHANGES = true;

    @Test
    public void testExtractPipelineEmptyPropertyAndNullProperty() throws Exception {
        FreeStyleProject job = jenkins.createFreeStyleProject("job");

        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", job);
        assertEquals(1, pipeline.getStages().size());
        assertEquals("job", pipeline.getStages().get(0).getName());
        assertEquals("job", pipeline.getStages().get(0).getTasks().get(0).getName());

        job.addProperty(new PipelineProperty("", "", ""));

        pipeline = Pipeline.extractPipeline("Pipeline", job);
        assertEquals(1, pipeline.getStages().size());
        assertEquals("job", pipeline.getStages().get(0).getName());
        assertEquals("job", pipeline.getStages().get(0).getTasks().get(0).getName());
    }

    @Test
    public void testExtractPipeline() throws Exception {
        final FreeStyleProject compile = jenkins.createFreeStyleProject("comp");
        final FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");
        final FreeStyleProject test = jenkins.createFreeStyleProject("test");

        compile.addProperty(new PipelineProperty("Compile", "Build", ""));
        compile.save();

        deploy.addProperty(new PipelineProperty("Deploy", "Deploy", ""));
        deploy.save();
        test.addProperty(new PipelineProperty("Test", "Test", ""));
        test.save();

        compile.getPublishersList().add(new BuildTrigger("test", false));
        test.getPublishersList().add(new BuildTrigger("deploy", false));

        jenkins.getInstance().rebuildDependencyGraph();


        Pipeline pipeline = Pipeline.extractPipeline("Piper", compile);

        assertNotNull(pipeline);
        assertEquals("Piper", pipeline.getName());
        assertEquals(3, pipeline.getStages().size());

        Stage buildStage = pipeline.getStages().get(0);
        assertEquals("Build", buildStage.getName());
        assertEquals(1, buildStage.getTasks().size());
        assertEquals("Compile", buildStage.getTasks().get(0).getName());
        assertEquals("comp", buildStage.getTasks().get(0).getId());

        Stage testStage = pipeline.getStages().get(1);
        assertEquals("Test", testStage.getName());
        assertEquals(1, testStage.getTasks().size());
        assertEquals("Test", testStage.getTasks().get(0).getName());
        assertEquals("test", testStage.getTasks().get(0).getId());

        Stage deployStage = pipeline.getStages().get(2);
        assertEquals("Deploy", deployStage.getName());
        assertEquals(1, deployStage.getTasks().size());
        assertEquals("Deploy", deployStage.getTasks().get(0).getName());
        assertEquals("deploy", deployStage.getTasks().get(0).getId());
    }

    @Test
    public void testExtractSimpleForkJoinPipeline() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        build.addProperty(new PipelineProperty(null, "build", ""));
        FreeStyleProject deploy1 = jenkins.createFreeStyleProject("deploy1");
        deploy1.addProperty(new PipelineProperty(null, "CI", ""));
        FreeStyleProject deploy2 = jenkins.createFreeStyleProject("deploy2");
        deploy2.addProperty(new PipelineProperty(null, "CI", ""));
        FreeStyleProject deploy3 = jenkins.createFreeStyleProject("deploy3");
        deploy3.addProperty(new PipelineProperty(null, "QA", ""));

        build.getPublishersList().add(new BuildTrigger("deploy1,deploy2", false));
        deploy1.getPublishersList().add(new BuildTrigger("deploy3", false));
        deploy2.getPublishersList().add(new BuildTrigger("deploy3", false));

        jenkins.getInstance().rebuildDependencyGraph();

        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", build);

        assertEquals(3, pipeline.getStages().size());
        assertEquals(1, pipeline.getStages().get(2).getTasks().size());
        assertEquals("deploy3", pipeline.getStages().get(2).getTasks().get(0).getName());
    }

    @Test
    public void testExtractPipelineWithSubProjects() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        build.addProperty(new PipelineProperty("Build", "Build", ""));
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");
        sonar.addProperty(new PipelineProperty("Sonar", "Build", ""));

        FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");
        deploy.addProperty(new PipelineProperty("Deploy", "QA", ""));

        build.getBuildersList().add(
                new TriggerBuilder(
                        new BlockableBuildTriggerConfig("sonar",
                                new BlockingBehaviour("never", "never", "never"), null)));
        build.getPublishersList().add(new BuildTrigger("deploy", false));

        jenkins.getInstance().rebuildDependencyGraph();

        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", build);
        assertEquals(2, pipeline.getStages().size());
        assertEquals(2, pipeline.getStages().get(0).getTasks().size());
        assertEquals(1, pipeline.getStages().get(1).getTasks().size());
    }

    @Test
    public void testCreatePipelineAggregatedSharedTask() throws Exception {
        final FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        final FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        final FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar1");
        final FreeStyleProject test = jenkins.createFreeStyleProject("test");
        jenkins.createFreeStyleProject("prod");
        build1.getPublishersList().add(new BuildTrigger("sonar1,test", true));
        build2.getPublishersList().add(new BuildTrigger("sonar1", true));
        test.getPublishersList().add(new BuildPipelineTrigger("prod", null));

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        final Pipeline pipe1 = Pipeline.extractPipeline("pipe1", build1);
        final Pipeline pipe2 = Pipeline.extractPipeline("pipe2", build2);

        Pipeline aggregated1 = pipe1.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        Pipeline aggregated2 = pipe2.createPipelineAggregatedWithChangesShown(jenkins.getInstance());

        assertNull(aggregated1.getStages().get(0).getVersion());
        assertNull(aggregated2.getStages().get(0).getVersion());

        assertTrue(aggregated1.getStages().get(0).getTasks().get(0).getStatus().isIdle());
        assertTrue(aggregated2.getStages().get(0).getTasks().get(0).getStatus().isIdle());

        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());

        assertEquals("job/sonar1/", aggregated1.getStages().get(3).getTasks().get(0).getLink());

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertNotNull(sonar.getLastBuild());

        assertEquals(4, pipe1.getStages().size());
        assertEquals(2, pipe2.getStages().size());

        aggregated1 = pipe1.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        aggregated2 = pipe2.createPipelineAggregatedWithChangesShown(jenkins.getInstance());

        assertEquals("#1", aggregated1.getStages().get(1).getVersion());
        assertEquals("job/sonar1/1/", aggregated1.getStages().get(3).getTasks().get(0).getLink());
        assertEquals("1", aggregated1.getStages().get(1).getTasks().get(0).getBuildId());

        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isSuccess());

        assertEquals(true, aggregated2.getStages().get(1).getTasks().get(0).getStatus().isIdle());
        assertEquals("job/sonar1/", aggregated2.getStages().get(1).getTasks().get(0).getLink());
        assertNull(aggregated2.getStages().get(1).getTasks().get(0).getBuildId());


        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isIdle());

        jenkins.buildAndAssertSuccess(build2);
        jenkins.waitUntilNoActivity();

        aggregated1 = pipe1.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        aggregated2 = pipe2.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());

        assertEquals("#1", aggregated1.getStages().get(1).getVersion());
        assertEquals("#1", aggregated2.getStages().get(1).getVersion());

        assertEquals(true, aggregated2.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("job/sonar1/2/", aggregated2.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("2", aggregated2.getStages().get(1).getTasks().get(0).getBuildId());

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();

        aggregated1 = pipe1.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        aggregated2 = pipe2.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());


        assertEquals("#2", aggregated1.getStages().get(1).getVersion());
        assertEquals("#1", aggregated2.getStages().get(1).getVersion());

        assertEquals("job/sonar1/3/", aggregated1.getStages().get(3).getTasks().get(0).getLink());
        assertEquals("3", aggregated1.getStages().get(3).getTasks().get(0).getBuildId());

        assertEquals("job/sonar1/2/", aggregated2.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("2", aggregated2.getStages().get(1).getTasks().get(0).getBuildId());


        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isIdle());

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertTrue(aggregated1.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("#2", aggregated1.getStages().get(1).getVersion());
        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isIdle());

        StandardBuildCard card = new StandardBuildCard();
        card.triggerManualBuild(jenkins.jenkins, 1, "prod", "test");

        jenkins.waitUntilNoActivity();
        aggregated1 = pipe1.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isSuccess());
        assertEquals("#1", aggregated1.getStages().get(2).getVersion());
    }

    @Test
    @Ignore("Looks like support for two manual trigger for one build has been broken in BPP 1.4.3")
    public void testAggregatedStageWithTwoManualTasks() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject ci1 = jenkins.createFreeStyleProject("ci1");
        FreeStyleProject ci2 = jenkins.createFreeStyleProject("ci2");
        ci1.addProperty(new PipelineProperty("ci1", "CI1", ""));
        ci2.addProperty(new PipelineProperty("ci2", "CI1", ""));
        build.getPublishersList().add(new BuildPipelineTrigger("ci1", null));
        build.getPublishersList().add(new BuildPipelineTrigger("ci2", null));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();

        assertNotNull(build.getLastBuild());

        BuildPipelineView view =
                new BuildPipelineView("", "", new DownstreamProjectGridBuilder("build"), "1", false, null);
        view.triggerManualBuild(1, "ci1", "build");

        jenkins.waitUntilNoActivity();
        assertNotNull(ci1.getLastBuild());
        assertNull(ci2.getLastBuild());

        Pipeline pipeline = Pipeline.extractPipeline("test", build);
        Pipeline aggregated = pipeline.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        assertNotNull(aggregated);
        assertEquals("ci1", aggregated.getStages().get(1).getTasks().get(0).getName());
        assertEquals("ci2", aggregated.getStages().get(1).getTasks().get(1).getName());
        assertEquals("SUCCESS", aggregated.getStages().get(1).getTasks().get(0).getStatus().toString());
        assertEquals("IDLE", aggregated.getStages().get(1).getTasks().get(1).getStatus().toString());
        assertEquals("#1", aggregated.getStages().get(1).getVersion());

        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();

        aggregated = pipeline.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        assertNotNull(aggregated);
        assertEquals("#2", build.getLastBuild().getDisplayName());
        assertEquals("SUCCESS", aggregated.getStages().get(1).getTasks().get(0).getStatus().toString());
        assertEquals("IDLE", aggregated.getStages().get(1).getTasks().get(1).getStatus().toString());
        assertEquals("#1", aggregated.getStages().get(1).getVersion());

        view.triggerManualBuild(2, "ci2", "build");
        jenkins.waitUntilNoActivity();
        aggregated = pipeline.createPipelineAggregatedWithoutChangesShown(jenkins.getInstance());
        assertNotNull(aggregated);
        assertEquals("IDLE", aggregated.getStages().get(1).getTasks().get(0).getStatus().toString());
        assertEquals("SUCCESS", aggregated.getStages().get(1).getTasks().get(1).getStatus().toString());
        assertEquals("#2", aggregated.getStages().get(1).getVersion());
    }

    @Test
    public void testCreatePipelineLatest() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        build.addProperty(new PipelineProperty("", "Build", ""));
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");
        sonar.addProperty(new PipelineProperty("Sonar", "Build", ""));
        FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");
        deploy.addProperty(new PipelineProperty("Deploy", "CI", ""));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", build);
        assertNotNull(pipeline);
        assertEquals("Pipeline", pipeline.getName());
        assertEquals(1, pipeline.getStages().size());

        Stage buildStage = pipeline.getStages().get(0);
        assertEquals("Build", buildStage.getName());
        assertEquals(1, buildStage.getTasks().size());
        assertEquals("build", buildStage.getTasks().get(0).getName());

        build.getPublishersList().add(new BuildTrigger("sonar,deploy", false));
        jenkins.getInstance().rebuildDependencyGraph();

        pipeline = Pipeline.extractPipeline("Pipeline", build);

        buildStage = pipeline.getStages().get(0);
        assertEquals("Build", buildStage.getName());
        assertEquals(2, buildStage.getTasks().size());
        assertEquals("build", buildStage.getTasks().get(0).getName());
        assertEquals("Sonar", buildStage.getTasks().get(1).getName());

        Stage ciStage = pipeline.getStages().get(1);
        assertEquals("CI", ciStage.getName());
        assertEquals(1, ciStage.getTasks().size());
        assertEquals("Deploy", ciStage.getTasks().get(0).getName());


        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();

        Pipeline latest = createPipelineLatest(pipeline, jenkins.getInstance());

        assertNotNull(latest);

        assertTrue(latest.getStages().get(0).getTasks().get(0).getStatus().isSuccess());
        assertTrue(latest.getStages().get(0).getTasks().get(1).getStatus().isSuccess());
        assertTrue(latest.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("job/build/1/", latest.getStages().get(0).getTasks().get(0).getLink());
        assertEquals(0, latest.getStages().get(0).getColumn());
        assertEquals(1, latest.getStages().get(1).getColumn());
    }

    @Test
    public void testPipelineLatestDownstreamIsDisabled() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        FreeStyleProject disabled = jenkins.createFreeStyleProject("disabled");
        disabled.makeDisabled(true);
        build.getPublishersList().add(new BuildTrigger("disabled", false));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();
        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", build);
        Pipeline latest = createPipelineLatest(pipeline, jenkins.getInstance());
        assertNotNull(latest);
        assertEquals(2, latest.getStages().size());
        assertEquals("SUCCESS", latest.getStages().get(0).getTasks().get(0).getStatus().toString());
        assertEquals("DISABLED", latest.getStages().get(1).getTasks().get(0).getStatus().toString());
    }

    @Test
    public void testFirstUpstreamBuildFirstProjectHasJustOneUpstreamJob() throws Exception {
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        final FreeStyleProject upstream = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject build = jenkins.createFreeStyleProject("build");
        upstream.getPublishersList().add(new BuildTrigger("build", false));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.buildAndAssertSuccess(upstream);
        jenkins.waitUntilNoActivity();

        assertNotNull(upstream.getLastBuild());
        assertNotNull(build.getLastBuild());

        assertEquals(build.getLastBuild(), BuildUtil.getFirstUpstreamBuild(build.getLastBuild(), build));
        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", build);
        Component component = new Component("Component", "build", null, false, 3, PAGING_DISABLED, 1);
        List<Pipeline> pipelines =
                pipeline.createPipelineLatest(1, Jenkins.getInstance(), PAGING_DISABLED, SHOW_CHANGES, component);
        assertEquals(1, pipelines.size());
        assertEquals(1, pipelines.get(0).getTriggeredBy().size());
        assertEquals(TriggerCause.TYPE_UPSTREAM, pipelines.get(0).getTriggeredBy().get(0).getType());
    }

    @Test
    public void getPipelineLatestWithDifferentFolders() throws Exception {
        MockFolder folder1 = jenkins.createFolder("folder1");
        MockFolder folder2 = jenkins.createFolder("folder2");
        final FreeStyleProject job1 = folder1.createProject(FreeStyleProject.class, "job1");
        final FreeStyleProject job2 = folder2.createProject(FreeStyleProject.class, "job2");

        job1.getPublishersList().add(new BuildTrigger("folder2/job2", false));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        Pipeline prototype = Pipeline.extractPipeline("Folders", job1);

        assertNotNull(prototype);

        jenkins.buildAndAssertSuccess(job1);
        jenkins.waitUntilNoActivity();

        assertNotNull(job1.getLastBuild());
        assertNotNull(job2.getLastBuild());

        Pipeline pipeline = createPipelineLatest(prototype, folder1);
        assertNotNull(pipeline);
        assertEquals(2, pipeline.getStages().size());
        assertEquals("folder1/job1", pipeline.getStages().get(0).getTasks().get(0).getId());
        assertEquals("folder2/job2", pipeline.getStages().get(1).getTasks().get(0).getId());
        assertEquals(0, pipeline.getStages().get(0).getColumn());
        assertEquals(1, pipeline.getStages().get(1).getColumn());
    }

    @Test
    public void testForkJoin() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        FreeStyleProject projectC = jenkins.createFreeStyleProject("C");
        FreeStyleProject projectD = jenkins.createFreeStyleProject("D");
        projectA.getPublishersList().add(new BuildTrigger("B,C", false));
        projectB.getPublishersList().add(new BuildTrigger("D", false));
        projectC.getPublishersList().add(new BuildTrigger("D", false));
        projectD.getPublishersList().add(
                new JoinTrigger(new DescribableList<>(Saveable.NOOP), "", false));
        jenkins.getInstance().rebuildDependencyGraph();
        Pipeline prototype = Pipeline.extractPipeline("ForkJoin", projectA);
        assertNotNull(prototype);
        assertEquals(4, prototype.getStages().size());

        assertEquals(0, prototype.getStages().get(0).getColumn());
        assertEquals(0, prototype.getStages().get(0).getRow());
        assertEquals(1, prototype.getStages().get(1).getColumn());
        assertEquals(0, prototype.getStages().get(1).getRow());
        assertEquals(2, prototype.getStages().get(2).getColumn());
        assertEquals(0, prototype.getStages().get(2).getRow());
        assertEquals(1, prototype.getStages().get(3).getColumn());
        assertEquals(1, prototype.getStages().get(3).getRow());
    }

    @Test
    public void getPipelineLatestWithSameFolders() throws Exception {
        MockFolder folder1 = jenkins.createFolder("folder1");
        final FreeStyleProject job1 = folder1.createProject(FreeStyleProject.class, "job1");
        final FreeStyleProject job2 = folder1.createProject(FreeStyleProject.class, "job2");

        job1.getPublishersList().add(new BuildTrigger("folder1/job2", false));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        Pipeline prototype = Pipeline.extractPipeline("Folders", job1);

        assertNotNull(prototype);

        jenkins.buildAndAssertSuccess(job1);
        jenkins.waitUntilNoActivity();

        assertNotNull(job1.getLastBuild());
        assertNotNull(job2.getLastBuild());

        Pipeline pipeline = createPipelineLatest(prototype, folder1);
        assertNotNull(pipeline);
        assertEquals(2, pipeline.getStages().size());
        assertEquals("folder1/job1", pipeline.getStages().get(0).getTasks().get(0).getId());
        assertEquals("folder1/job2", pipeline.getStages().get(1).getTasks().get(0).getId());

        assertTrue(pipeline.getStages().get(0).getTasks().get(0).getStatus().isSuccess());
        assertTrue(pipeline.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
    }

    @Test
    public void getPipelineLatestWithNestedFolders() throws Exception {
        MockFolder folder1 = jenkins.createFolder("folder1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "subfolder");

        final FreeStyleProject job1 = folder2.createProject(FreeStyleProject.class, "job1");
        final FreeStyleProject job2 = folder1.createProject(FreeStyleProject.class, "job2");

        job1.getPublishersList().add(new BuildTrigger("folder1/job2", false));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        Pipeline prototype = Pipeline.extractPipeline("Folders", job1);

        assertNotNull(prototype);

        jenkins.buildAndAssertSuccess(job1);
        jenkins.waitUntilNoActivity();

        assertNotNull(job1.getLastBuild());
        assertNotNull(job2.getLastBuild());

        Pipeline pipeline = createPipelineLatest(prototype, folder1);
        assertNotNull(pipeline);
        assertEquals(2, pipeline.getStages().size());
        assertEquals("folder1/subfolder/job1", pipeline.getStages().get(0).getTasks().get(0).getId());
        assertEquals("folder1/job2", pipeline.getStages().get(1).getTasks().get(0).getId());

        assertTrue(pipeline.getStages().get(0).getTasks().get(0).getStatus().isSuccess());
        assertTrue(pipeline.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
    }

    /**
     * A -> B -> D -> E
     *        -> C     
     * <p/>
     * Javascript in view needs to have a sorted list of stages based
     * on row and column the stage has been placed in.
     */
    @Issue("JENKINS-22211")
    @Test
    public void testGetPipelinesWhereRowsWillBeGambled() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        jenkins.createFreeStyleProject("c");
        final FreeStyleProject projectD = jenkins.createFreeStyleProject("d");
        jenkins.createFreeStyleProject("e");

        projectA.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("b", new BlockingBehaviour("never", "never", "never"), null)));
        projectB.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("c,d", new BlockingBehaviour("never", "never", "never"), null)));
        projectD.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("e", new BlockingBehaviour("never", "never", "never"), null)));

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        Pipeline pipeline = Pipeline.extractPipeline("test", projectA);

        assertEquals("a", pipeline.getStages().get(0).getName());
        assertEquals(0, pipeline.getStages().get(0).getRow());
        assertEquals(0, pipeline.getStages().get(0).getColumn());

        assertEquals("b", pipeline.getStages().get(1).getName());
        assertEquals(0, pipeline.getStages().get(1).getRow());
        assertEquals(1, pipeline.getStages().get(1).getColumn());

        assertEquals("d", pipeline.getStages().get(2).getName());
        assertEquals(0, pipeline.getStages().get(2).getRow());
        assertEquals(2, pipeline.getStages().get(2).getColumn());

        assertEquals("e", pipeline.getStages().get(3).getName());
        assertEquals(0, pipeline.getStages().get(3).getRow());
        assertEquals(3, pipeline.getStages().get(3).getColumn());

        assertEquals("c", pipeline.getStages().get(4).getName());
        assertEquals(1, pipeline.getStages().get(4).getRow());
        assertEquals(2, pipeline.getStages().get(4).getColumn());
    }

    /**
     * A -> B -> D -> E -> F
     *        -> C      -> G
     *                  -> H
     * <p/>
     * Javascript in view needs to have a sorted list of stages based
     * on row and column the stage has been placed in.
     */
    @Test
    public void testGetPipelinesWhereEachRowHasMultipleStages() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("a");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("b");
        jenkins.createFreeStyleProject("c");
        final FreeStyleProject projectD = jenkins.createFreeStyleProject("d");
        final FreeStyleProject projectE = jenkins.createFreeStyleProject("e");
        jenkins.createFreeStyleProject("f");
        jenkins.createFreeStyleProject("g");
        jenkins.createFreeStyleProject("h");

        projectA.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("b", new BlockingBehaviour("never", "never", "never"), null)));
        projectB.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("c,d", new BlockingBehaviour("never", "never", "never"), null)));
        projectD.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("e", new BlockingBehaviour("never", "never", "never"), null)));
        projectE.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("f,g,h", new BlockingBehaviour("never", "never", "never"), null)));

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        Pipeline pipeline = Pipeline.extractPipeline("test", projectA);

        assertEquals("a", pipeline.getStages().get(0).getName());
        assertEquals(0, pipeline.getStages().get(0).getRow());
        assertEquals(0, pipeline.getStages().get(0).getColumn());

        assertEquals("b", pipeline.getStages().get(1).getName());
        assertEquals(0, pipeline.getStages().get(1).getRow());
        assertEquals(1, pipeline.getStages().get(1).getColumn());

        assertEquals("d", pipeline.getStages().get(2).getName());
        assertEquals(0, pipeline.getStages().get(2).getRow());
        assertEquals(2, pipeline.getStages().get(2).getColumn());

        assertEquals("e", pipeline.getStages().get(3).getName());
        assertEquals(0, pipeline.getStages().get(3).getRow());
        assertEquals(3, pipeline.getStages().get(3).getColumn());

        assertEquals("f", pipeline.getStages().get(4).getName());
        assertEquals(0, pipeline.getStages().get(4).getRow());
        assertEquals(4, pipeline.getStages().get(4).getColumn());

        assertEquals("c", pipeline.getStages().get(5).getName());
        assertEquals(1, pipeline.getStages().get(5).getRow());
        assertEquals(2, pipeline.getStages().get(5).getColumn());

        assertEquals("g", pipeline.getStages().get(6).getName());
        assertEquals(1, pipeline.getStages().get(6).getRow());
        assertEquals(4, pipeline.getStages().get(6).getColumn());

        assertEquals("h", pipeline.getStages().get(7).getName());
        assertEquals(2, pipeline.getStages().get(7).getRow());
        assertEquals(4, pipeline.getStages().get(7).getColumn());
    }

    /*
     * A --> B --> C --> D
     */
    @Test
    @Issue("JENKINS-22658")
    public void testRecursiveStages() throws Exception {

        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        projectA.addProperty(new PipelineProperty("A", "A", ""));
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectB.addProperty(new PipelineProperty("B", "B", ""));
        FreeStyleProject projectC = jenkins.createFreeStyleProject("C");
        projectC.addProperty(new PipelineProperty("C", "C", ""));
        FreeStyleProject projectD = jenkins.createFreeStyleProject("D");
        projectD.addProperty(new PipelineProperty("D", "B", ""));

        projectA.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("B", ResultCondition.SUCCESS, new ArrayList<>())));
        projectB.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("C", ResultCondition.SUCCESS, new ArrayList<>())));
        projectC.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("D", ResultCondition.SUCCESS, new ArrayList<>())));

        jenkins.getInstance().rebuildDependencyGraph();

        try {
            Pipeline.extractPipeline("Test", projectA);
            fail();
        } catch (StackOverflowError e) {
            fail("Should not throw StackOverflowError");
        } catch (PipelineException e) {
            //Should throw this
        }
    }

    @Test
    public void testShouldShowPipelineInstanceInQueue() throws Exception {
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        Pipeline prototype = Pipeline.extractPipeline("Pipe", projectA);
        projectA.scheduleBuild(2, new Cause.UserIdCause());
        Component component = new Component(
                "Component",prototype.getFirstProject().getFullName(), null, false, 3, PAGING_DISABLED, 1);
        List<Pipeline> pipelines = prototype
                .createPipelineLatest(5, Jenkins.getInstance(), PAGING_DISABLED, SHOW_CHANGES, component);
        assertEquals(1, pipelines.size());
    }

    private Pipeline createPipelineLatest(Pipeline pipeline, ItemGroup itemGroup) throws PipelineException {
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        Component component = new Component(
                "Component", pipeline.getFirstProject().getFullName(), null, false, 3, PAGING_DISABLED, 1);
        List<Pipeline> pipelines = pipeline
                .createPipelineLatest(1, itemGroup, PAGING_DISABLED, SHOW_CHANGES, component);
        assertFalse(pipelines.isEmpty());
        return pipelines.get(0);
    }

    @Test
    public void testCalculatePipelineRoutesSimpleRoute() throws Exception {
        /*
        stage1   stage2   stage3
        task1 -- task2 -- task3
         */
        Stage stage1 = mock(Stage.class);
        Stage stage2 = mock(Stage.class);
        Stage stage3 = mock(Stage.class);
        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);
        Task task3 = mock(Task.class);
        when(task1.getId()).thenReturn("task1");
        when(task2.getId()).thenReturn("task2");
        when(task3.getId()).thenReturn("task3");
        when(task2.getDownstreamTasks()).thenReturn(Collections.singletonList("task3"));
        when(task1.getDownstreamTasks()).thenReturn(Collections.singletonList("task2"));
        when(stage1.getTasks()).thenReturn(Collections.singletonList(task1));
        when(stage2.getTasks()).thenReturn(Collections.singletonList(task2));
        when(stage3.getTasks()).thenReturn(Collections.singletonList(task3));

        FreeStyleProject project = jenkins.createFreeStyleProject("A");
        Pipeline pipeline = new Pipeline("SimpleRoute", project, null, Arrays.asList(stage1, stage2, stage3));
        List<Route> allRoutes = new ArrayList<>();
        pipeline.calculatePipelineRoutes(task1, null, allRoutes);
        /*
        task1 -> task2 -> task3
         */
        assertEquals(1, allRoutes.size());
    }

    @Test
    public void testCalculatePipelineRoutesComplexRoutes() throws Exception {
        /*
        stage1   stage2   stage3   stage4
        task1 -- task2 -- task4 -- task6
              |_ task3 |_ task5     |_ task7
         */
        Stage stage1 = mock(Stage.class);
        Stage stage2 = mock(Stage.class);
        Stage stage3 = mock(Stage.class);
        Stage stage4 = mock(Stage.class);
        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);
        Task task3 = mock(Task.class);
        Task task4 = mock(Task.class);
        Task task5 = mock(Task.class);
        Task task6 = mock(Task.class);
        Task task7 = mock(Task.class);
        Status status1 = mock(Status.class);
        Status status2 = mock(Status.class);
        Status status3 = mock(Status.class);
        Status status4 = mock(Status.class);
        Status status5 = mock(Status.class);
        Status status6 = mock(Status.class);
        Status status7 = mock(Status.class);
        when(task1.getId()).thenReturn("task1");
        when(task2.getId()).thenReturn("task2");
        when(task3.getId()).thenReturn("task3");
        when(task4.getId()).thenReturn("task4");
        when(task5.getId()).thenReturn("task5");
        when(task6.getId()).thenReturn("task6");
        when(task7.getId()).thenReturn("task7");
        when(status1.getDuration()).thenReturn(100L);
        when(status2.getDuration()).thenReturn(200L);
        when(status3.getDuration()).thenReturn(300L);
        when(status4.getDuration()).thenReturn(400L);
        when(status5.getDuration()).thenReturn(500L);
        when(status6.getDuration()).thenReturn(600L);
        when(status7.getDuration()).thenReturn(700L);
        when(task1.getStatus()).thenReturn(status1);
        when(task2.getStatus()).thenReturn(status2);
        when(task3.getStatus()).thenReturn(status3);
        when(task4.getStatus()).thenReturn(status4);
        when(task5.getStatus()).thenReturn(status5);
        when(task6.getStatus()).thenReturn(status6);
        when(task7.getStatus()).thenReturn(status7);
        when(task6.getDownstreamTasks()).thenReturn(Collections.singletonList("task7"));
        when(task4.getDownstreamTasks()).thenReturn(Collections.singletonList("task6"));
        when(task2.getDownstreamTasks()).thenReturn(Arrays.asList("task4", "task5"));
        when(task1.getDownstreamTasks()).thenReturn(Arrays.asList("task2", "task3"));
        when(stage1.getTasks()).thenReturn(Collections.singletonList(task1));
        when(stage2.getTasks()).thenReturn(Arrays.asList(task2, task3));
        when(stage3.getTasks()).thenReturn(Arrays.asList(task4, task5));
        when(stage4.getTasks()).thenReturn(Arrays.asList(task6, task7));

        FreeStyleProject project = jenkins
                .createFreeStyleProject("A");
        Pipeline pipeline =
                new Pipeline("TotalBuildTime", project, null, Arrays.asList(stage1, stage2, stage3, stage4));
        /*
        task1 -> task3: 100 + 300 = 400L
        task1 -> task2 -> task5: 100 + 200 + 500 = 800L
        task1 -> task2 -> task4 -> task6 -> task7: 100 + 200 + 400 + 600 + 700 = 2000L
         */
        pipeline.calculateTotalBuildTime();
        assertEquals(2000L, pipeline.getTotalBuildTime());
    }

    @Test
    public void testCalculateTotalBuildTimeNoStages() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("A");
        Pipeline pipeline = new Pipeline("NoStages", project, null, new ArrayList<>());
        pipeline.calculateTotalBuildTime();
        assertEquals(0L, pipeline.getTotalBuildTime());
    }

    @Test
    @Issue("JENKINS-30043")
    public void testSubProjectsFirst() throws Exception {
        FreeStyleProject jobA = jenkins.createFreeStyleProject("Job A");
        jobA.addProperty(new PipelineProperty(null, "Stage", null));
        FreeStyleProject util1 = jenkins.createFreeStyleProject("Job Util 1");
        util1.addProperty(new PipelineProperty(null, "Stage", null));
        FreeStyleProject util2 = jenkins.createFreeStyleProject("Job Util 2");
        util2.addProperty(new PipelineProperty(null, "Stage", null));
        FreeStyleProject jobC = jenkins.createFreeStyleProject("Job C");
        jobC.addProperty(new PipelineProperty(null, "Stage", null));

        jobA.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("Job Util 1", new BlockingBehaviour("never", "never", "never"), null)));
        jobA.getBuildersList().add(new TriggerBuilder(
                new BlockableBuildTriggerConfig("Job Util 2", new BlockingBehaviour("never", "never", "never"), null)));
        jobA.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(
                new BuildTriggerConfig("Job C", ResultCondition.SUCCESS,
                        new ArrayList<>())));

        jenkins.getInstance().rebuildDependencyGraph();

        Pipeline pipeline = Pipeline.extractPipeline("Pipeline", jobA);

        assertEquals("Job A", pipeline.getStages().get(0).getTasks().get(0).getId());
        assertEquals("Job Util 1", pipeline.getStages().get(0).getTasks().get(1).getId());
        assertEquals("Job Util 2", pipeline.getStages().get(0).getTasks().get(2).getId());
        assertEquals("Job C", pipeline.getStages().get(0).getTasks().get(3).getId());
    }
}
