package se.diabol.pipefitter;

import com.google.common.collect.ImmutableMap;
import hudson.model.AbstractProject;
import org.testng.annotations.Test;
import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static se.diabol.pipefitter.model.status.StatusFactory.idle;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactoryTest
{
    final AbstractProject compileJob = createMockJob("comp", "Compile", "Build");
    final AbstractProject analyzeJob = createMockJob("anal", "Analyze", "Build");
    final AbstractProject testJob = createMockJob("test", "Test", null);
    final AbstractProject altTestJob = createMockJob("test2", "Test fnutt", "Test");
    final AbstractProject deployJob = createMockJob("deploy", "Deploy", null);

    @Test
    public void testExtractPipeline() throws Exception
    {
        Map<AbstractProject, List<AbstractProject>> jobGraph
                = ImmutableMap.<AbstractProject, List<AbstractProject>>builder()
                              .put(compileJob, asList(analyzeJob, testJob))
                              .build();

        PipelineFactory pipelineFactory = createPipelineFactory(jobGraph);
        Pipeline pipeline = pipelineFactory.extractPipeline("Piper", compileJob);

        assertEquals(pipeline,
                     new Pipeline("Piper",
                                  asList(new Stage("Build", asList(new Task("comp", "Compile", idle()),
                                                                   new Task("anal", "Analyze", idle()))),
                                         new Stage("Test", asList(new Task("test", "Test", idle()))))));
    }

    @Test
    public void testExtractPipelineWithJoin() throws Exception
    {
        Map<AbstractProject, List<AbstractProject>> jobGraph
                = ImmutableMap.<AbstractProject, List<AbstractProject>>builder()
                              .put(compileJob, asList(testJob, altTestJob))
                              .put(testJob, singletonList(deployJob))
                              .put(altTestJob, singletonList(deployJob))
                              .build();

        PipelineFactory pipelineFactory = createPipelineFactory(jobGraph);
        Pipeline pipeline = pipelineFactory.extractPipeline("Piper", compileJob);

        assertEquals(pipeline,
                     new Pipeline("Piper",
                                  asList(new Stage("Build", asList(new Task("comp", "Compile", idle()))),
                                         new Stage("Test", asList(new Task("test", "Test", idle()),
                                                                  new Task("test2", "Test fnutt", idle()))),
                                         new Stage("Deploy", asList(new Task("deploy", "Deploy", idle()))))));
    }

    private PipelineFactory createPipelineFactory(final Map<AbstractProject, List<AbstractProject>> jobGraph)
    {
        return new PipelineFactory() {
            @SuppressWarnings("unchecked") @Override
            List<AbstractProject<?, ?>> getDownstreamProjects(AbstractProject project) {
                List<AbstractProject> downstreamProjects = jobGraph.get(project);
                return (List<AbstractProject<?,?>>) ((downstreamProjects != null) ? downstreamProjects : emptyList());
            }
        };
    }

    private static AbstractProject createMockJob(String name, String displayName, String stageName)
    {
        PipelineProperty property = displayName != null || stageName != null
                                    ? new PipelineProperty(nullToEmpty(displayName), nullToEmpty(stageName))
                                    : null;

        AbstractProject project = mock(AbstractProject.class);
        when(project.getProperty(PipelineProperty.class)).thenReturn(property);
        when(project.getName()).thenReturn(name);
        when(project.getDisplayName()).thenReturn(displayName);
        return project;
    }
}
