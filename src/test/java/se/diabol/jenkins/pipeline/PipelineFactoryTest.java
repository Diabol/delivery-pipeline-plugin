package se.diabol.jenkins.pipeline;

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
import static se.diabol.jenkins.pipeline.model.status.StatusFactory.idle;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactoryTest  {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testExtractPipelineWithJoin() throws Exception {
        FreeStyleProject compile =  jenkins.createFreeStyleProject("comp");
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
                                new Stage("Deploy", asList(new Task("deploy", "Deploy", idle(), "", null))))));


    }


    @Test
    public void testCreatePipelineAggregated() throws Exception {
        FreeStyleProject build1 = jenkins.createFreeStyleProject("build1");
        FreeStyleProject build2 = jenkins.createFreeStyleProject("build2");
        FreeStyleProject sonar = jenkins.createFreeStyleProject("sonar1");
        build1.getPublishersList().add(new BuildTrigger("sonar1", true));
        build2.getPublishersList().add(new BuildTrigger("sonar1", true));
        build1.save();
        build2.save();
        jenkins.getInstance().rebuildDependencyGraph();
        jenkins.setQuietPeriod(0);

        jenkins.buildAndAssertSuccess(build1);
        jenkins.waitUntilNoActivity();
        assertNotNull(sonar.getLastBuild());

        PipelineFactory factory = new PipelineFactory();
        final Pipeline pipe1 = factory.extractPipeline("pipe1", build1);
        final Pipeline pipe2 = factory.extractPipeline("pipe2", build2);
        assertEquals(pipe1.getStages().size(),2);
        assertEquals(pipe2.getStages().size(),2);
        assertNotNull(sonar.getBuild("1"));

        Pipeline aggregated1 = factory.createPipelineAggregated(pipe1);
        Pipeline aggregated2 = factory.createPipelineAggregated(pipe2);

        assertEquals(aggregated1.getStages().get(0).getVersion(), "#1");
        assertEquals(aggregated2.getStages().get(0).getStatus().isIdle(), true);




    }
}
