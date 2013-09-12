package se.diabol.jenkins.pipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildTrigger;
import hudson.util.OneShotEvent;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.*;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;
import se.diabol.jenkins.pipeline.model.status.Status;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static se.diabol.jenkins.pipeline.model.status.StatusFactory.idle;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactoryTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testExtractPipelineWithJoin() throws Exception {
        FreeStyleProject compile = jenkins.createFreeStyleProject("comp");
        FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");
        FreeStyleProject test = jenkins.createFreeStyleProject("test");

        compile.addProperty(new PipelineProperty("Compile", "Build"));
        compile.save();

        deploy.addProperty(new PipelineProperty("Deploy", "Deploy"));
        deploy.save();
        test.addProperty(new PipelineProperty("Test", "Test"));
        test.save();

        compile.getPublishersList().add(new BuildTrigger("test", false));
        test.getPublishersList().add(new BuildTrigger("deploy", false));

        jenkins.getInstance().rebuildDependencyGraph();


        Pipeline pipeline = PipelineFactory.extractPipeline("Piper", compile);

        assertEquals(pipeline,
                new Pipeline("Piper", null, null,
                        asList(new Stage("Build", asList(new Task("comp", "Compile", null, idle(), "", false, null))),
                                new Stage("Test", asList(new Task("test", "Test", null, idle(), "", false, null))),
                                new Stage("Deploy", asList(new Task("deploy", "Deploy", null, idle(), "", false, null)))), false));


    }


    @Test
    public void testCreatePipelineAggregatedSharedTask() throws Exception {
        FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar1");
        FreeStyleProject test = jenkins.createFreeStyleProject("test");
        jenkins.createFreeStyleProject("prod");
        build1.getPublishersList().add(new BuildTrigger("sonar1,test", true));
        build2.getPublishersList().add(new BuildTrigger("sonar1", true));
        test.getPublishersList().add(new BuildPipelineTrigger("prod", null));

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        final Pipeline pipe1 = PipelineFactory.extractPipeline("pipe1", build1);
        final Pipeline pipe2 = PipelineFactory.extractPipeline("pipe2", build2);

        Pipeline aggregated1 = PipelineFactory.createPipelineAggregated(pipe1);
        Pipeline aggregated2 = PipelineFactory.createPipelineAggregated(pipe2);

        assertNull(aggregated1.getStages().get(0).getVersion());
        assertNull(aggregated2.getStages().get(0).getVersion());

        assertTrue(aggregated1.getStages().get(0).getTasks().get(0).getStatus().isIdle());
        assertTrue(aggregated2.getStages().get(0).getTasks().get(0).getStatus().isIdle());

        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());


        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertNotNull(sonar.getLastBuild());

        assertEquals(4, pipe1.getStages().size());
        assertEquals(2, pipe2.getStages().size());
        assertNotNull(sonar.getBuild("1"));

        aggregated1 = PipelineFactory.createPipelineAggregated(pipe1);
        aggregated2 = PipelineFactory.createPipelineAggregated(pipe2);

        assertEquals("#1", aggregated1.getStages().get(1).getVersion());
        assertEquals("job/sonar1/1/", aggregated1.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("1", aggregated1.getStages().get(1).getTasks().get(0).getBuildId());

        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isSuccess());

        assertEquals(true, aggregated2.getStages().get(1).getTasks().get(0).getStatus().isIdle());
        assertEquals("job/sonar1", aggregated2.getStages().get(1).getTasks().get(0).getLink());
        assertNull(aggregated2.getStages().get(1).getTasks().get(0).getBuildId());


        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());

        jenkins.buildAndAssertSuccess(build2);
        jenkins.waitUntilNoActivity();

        aggregated1 = PipelineFactory.createPipelineAggregated(pipe1);
        aggregated2 = PipelineFactory.createPipelineAggregated(pipe2);

        assertEquals("#1", aggregated1.getStages().get(1).getVersion());
        assertEquals("#1", aggregated2.getStages().get(1).getVersion());

        assertEquals(true, aggregated2.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("job/sonar1/2/", aggregated2.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("2", aggregated2.getStages().get(1).getTasks().get(0).getBuildId());

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();

        aggregated1 = PipelineFactory.createPipelineAggregated(pipe1);
        aggregated2 = PipelineFactory.createPipelineAggregated(pipe2);


        assertEquals("#2", aggregated1.getStages().get(1).getVersion());
        assertEquals("#1", aggregated2.getStages().get(1).getVersion());

        assertEquals("job/sonar1/3/", aggregated1.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("3", aggregated1.getStages().get(1).getTasks().get(0).getBuildId());

        assertEquals("job/sonar1/2/", aggregated2.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("2", aggregated2.getStages().get(1).getTasks().get(0).getBuildId());


        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isSuccess());
        assertEquals("#2", aggregated1.getStages().get(2).getVersion());
        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());


        BuildPipelineView view = new BuildPipelineView("", "", new DownstreamProjectGridBuilder("build1"), "1", false, null);
        view.triggerManualBuild(1, "prod", "test");
        jenkins.waitUntilNoActivity();
        aggregated1 = PipelineFactory.createPipelineAggregated(pipe1);
        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isSuccess());
        assertEquals("#1", aggregated1.getStages().get(3).getVersion());


    }

    @Test
    public void testCreatePipelineLatest() throws Exception {
        FreeStyleProject build = jenkins.createFreeStyleProject("build");
        build.addProperty(new PipelineProperty("", "Build"));
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar");
        sonar.addProperty(new PipelineProperty("Sonar", "Build"));
        FreeStyleProject deploy = jenkins.createFreeStyleProject("deploy");
        deploy.addProperty(new PipelineProperty("Deploy", "CI"));
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        assertEquals(new Pipeline("Pipeline", null, null, asList(new Stage("Build", asList(new Task("build", "build", null, idle(), null,false, null)))), false), PipelineFactory.extractPipeline("Pipeline", build));


        build.getPublishersList().add(new BuildTrigger("sonar,deploy", false));
        jenkins.getInstance().rebuildDependencyGraph();

        Pipeline pipeline = PipelineFactory.extractPipeline("Pipeline", build);

        assertEquals(new Pipeline("Pipeline", null, null, asList(new Stage("Build", asList(new Task("build", "build", null, idle(), null, false, null), new Task("sonar", "Sonar",null, idle(), null, false, null))), new Stage("CI", asList(new Task("deploy", "Deploy", null, idle(), null, false, null)))), false), pipeline);
        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();

        Pipeline latest = PipelineFactory.createPipelineLatest(pipeline);

        assertNotNull(latest);

        assertTrue(latest.getStages().get(0).getTasks().get(0).getStatus().isSuccess());
        assertTrue(latest.getStages().get(0).getTasks().get(1).getStatus().isSuccess());
        assertTrue(latest.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("job/build/1/", latest.getStages().get(0).getTasks().get(0).getLink());
    }

    @Test
    public void testResolveStatusIdle() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        Status status = PipelineFactory.resolveStatus(project, null);
        assertTrue(status.isIdle());
        assertEquals("IDLE", status.toString());
        assertEquals(-1, status.getLastActivity());

    }

    @Test
    public void testResolveStatusDisabled() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.makeDisabled(true);
        Status status = PipelineFactory.resolveStatus(project, null);
        assertTrue(status.isDisabled());
        assertEquals("DISABLED", status.toString());
        assertEquals(-1, status.getLastActivity());

    }

    @Test
    public void testResolveStatusSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        jenkins.buildAndAssertSuccess(project);
        jenkins.waitUntilNoActivity();
        Status status = PipelineFactory.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isSuccess());
        assertEquals("SUCCESS", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());

    }

    @Test
    public void testResolveStatusFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new FailureBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = PipelineFactory.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isFailed());
        assertEquals("FAILED", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
    }


    @Test
    public void testResolveStatusUnstable() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new UnstableBuilder());
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = PipelineFactory.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isUnstable());
        assertEquals("UNSTABLE", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
    }


    @Test
    public void testResolveStatusAborted() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MockBuilder(Result.ABORTED));
        project.scheduleBuild2(0);
        jenkins.waitUntilNoActivity();
        Status status = PipelineFactory.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isCancelled());
        assertEquals("CANCELLED", status.toString());
        assertEquals(project.getLastBuild().getTimeInMillis(), status.getLastActivity());
    }


    @Test
    public void testResolveStatusQueued() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.scheduleBuild2(2);
        Status status = PipelineFactory.resolveStatus(project, null);
        assertTrue(status.isQueued());
        assertEquals("QUEUED", status.toString());
        jenkins.waitUntilNoActivity();
        status = PipelineFactory.resolveStatus(project, project.getLastBuild());
        assertTrue(status.isSuccess());
    }

    @Test
    public void testResolveStatusBuilding() throws Exception {
        final OneShotEvent buildStarted = new OneShotEvent();

        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                buildStarted.signal();
                Thread.currentThread().wait(1000);
                return true;
            }
        });

        project.scheduleBuild2(0);
        buildStarted.block(); // wait for the build to really start
        Status status = PipelineFactory.resolveStatus(project, project.getFirstBuild());
        jenkins.waitUntilNoActivity();
        assertTrue(status.isRunning());
    }

}
