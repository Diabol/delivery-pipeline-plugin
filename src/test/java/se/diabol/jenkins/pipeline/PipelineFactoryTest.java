package se.diabol.jenkins.pipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;

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


        PipelineFactory pipelineFactory = new PipelineFactory();
        Pipeline pipeline = pipelineFactory.extractPipeline("Piper", compile);

        assertEquals(pipeline,
                new Pipeline("Piper", null, null,
                        asList(new Stage("Build", asList(new Task("comp", "Compile", idle(), "", null))),
                                new Stage("Test", asList(new Task("test", "Test", idle(), "", null))),
                                new Stage("Deploy", asList(new Task("deploy", "Deploy", idle(), "", null)))), false));


    }


    @Test
    public void testCreatePipelineAggregatedSharedTask() throws Exception {
        FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar1");
        FreeStyleProject test = jenkins.createFreeStyleProject("test");
        FreeStyleProject prod = jenkins.createFreeStyleProject("prod");
        build1.getPublishersList().add(new BuildTrigger("sonar1,test", true));
        build2.getPublishersList().add(new BuildTrigger("sonar1", true));
        test.getPublishersList().add(new BuildPipelineTrigger("prod", null));

        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);
        PipelineFactory factory = new PipelineFactory();
        final Pipeline pipe1 = factory.extractPipeline("pipe1", build1);
        final Pipeline pipe2 = factory.extractPipeline("pipe2", build2);

        Pipeline aggregated1 = factory.createPipelineAggregated(pipe1);
        Pipeline aggregated2 = factory.createPipelineAggregated(pipe2);

        assertNull(aggregated1.getStages().get(0).getVersion());
        assertNull(aggregated2.getStages().get(0).getVersion());

        assertTrue(aggregated1.getStages().get(0).getTasks().get(0).getStatus().isIdle());
        assertTrue(aggregated2.getStages().get(0).getTasks().get(0).getStatus().isIdle());

        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());


        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertNotNull(sonar.getLastBuild());

        assertEquals(4,pipe1.getStages().size());
        assertEquals(2, pipe2.getStages().size());
        assertNotNull(sonar.getBuild("1"));

        aggregated1 = factory.createPipelineAggregated(pipe1);
        aggregated2 = factory.createPipelineAggregated(pipe2);

        assertEquals("#1", aggregated1.getStages().get(1).getVersion());
        assertEquals("job/sonar1/1/", aggregated1.getStages().get(1).getTasks().get(0).getLink());

        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isSuccess());

        assertEquals(true, aggregated2.getStages().get(1).getTasks().get(0).getStatus().isIdle());
        assertEquals("job/sonar1", aggregated2.getStages().get(1).getTasks().get(0).getLink());

        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());

        jenkins.buildAndAssertSuccess(build2);
        jenkins.waitUntilNoActivity();

        aggregated1 = factory.createPipelineAggregated(pipe1);
        aggregated2 = factory.createPipelineAggregated(pipe2);

        assertEquals("#1", aggregated1.getStages().get(1).getVersion());
        assertEquals("#1", aggregated2.getStages().get(1).getVersion());

        assertEquals(true, aggregated2.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("job/sonar1/2/", aggregated2.getStages().get(1).getTasks().get(0).getLink());


        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();

        aggregated1 = factory.createPipelineAggregated(pipe1);
        aggregated2 = factory.createPipelineAggregated(pipe2);


        assertEquals("#2", aggregated1.getStages().get(1).getVersion());
        assertEquals("#1", aggregated2.getStages().get(1).getVersion());

        assertEquals("job/sonar1/3/", aggregated1.getStages().get(1).getTasks().get(0).getLink());
        assertEquals("job/sonar1/2/", aggregated2.getStages().get(1).getTasks().get(0).getLink());


        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertTrue(aggregated1.getStages().get(2).getTasks().get(0).getStatus().isSuccess());
        assertEquals("#2", aggregated1.getStages().get(2).getVersion());
        assertTrue(aggregated1.getStages().get(3).getTasks().get(0).getStatus().isIdle());


        BuildPipelineView view = new BuildPipelineView("", "", new DownstreamProjectGridBuilder("build1"), "1", false, null );
        view.triggerManualBuild(1, "prod", "test");
        jenkins.waitUntilNoActivity();
        aggregated1 = factory.createPipelineAggregated(pipe1);
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

        PipelineFactory factory = new PipelineFactory();
        assertEquals(new Pipeline("Pipeline", null, null,asList(new Stage("Build", asList(new Task("build", "build", idle(), null, null)))), false), factory.extractPipeline("Pipeline", build));


        build.getPublishersList().add(new BuildTrigger("sonar,deploy", false));
        jenkins.getInstance().rebuildDependencyGraph();

        Pipeline pipeline = factory.extractPipeline("Pipeline", build);

        assertEquals(new Pipeline("Pipeline", null, null,asList(new Stage("Build", asList(new Task("build", "build", idle(), null, null), new Task("sonar", "Sonar", idle(), null, null))), new Stage("CI", asList(new Task("deploy", "Deploy", idle(), null, null)))), false), pipeline);
        jenkins.buildAndAssertSuccess(build);
        jenkins.waitUntilNoActivity();

        Pipeline latest = factory.createPipelineLatest(pipeline);

        assertNotNull(latest);

        assertTrue(latest.getStages().get(0).getTasks().get(0).getStatus().isSuccess());
        assertTrue(latest.getStages().get(0).getTasks().get(1).getStatus().isSuccess());
        assertTrue(latest.getStages().get(1).getTasks().get(0).getStatus().isSuccess());
        assertEquals("job/build/1/", latest.getStages().get(0).getTasks().get(0).getLink());



    }
}
