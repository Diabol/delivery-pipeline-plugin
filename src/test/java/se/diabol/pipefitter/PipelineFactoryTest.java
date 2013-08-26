package se.diabol.pipefitter;

import hudson.model.AbstractProject;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static se.diabol.pipefitter.model.status.StatusFactory.idle;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineFactoryTest
{
    @Test
    public void testExtractPipeline() throws Exception
    {
        final AbstractProject analyzeJob = createMockJob("anal", "Analyze", "Build");
        final AbstractProject testJob = createMockJob("test", "Test", null);
        final AbstractProject compileJob = createMockJob("comp", "Compile", "Build");

        PipelineFactory pipelineFactory = new PipelineFactory() {
            @SuppressWarnings("unchecked") @Override
            List<AbstractProject<?, ?>> getDownstreamProjects(AbstractProject project) {
                return (List<AbstractProject<?,?>>) (project == compileJob
                                                     ? asList(analyzeJob, testJob)
                                                     : Collections.emptyList());
            }
        };

        Pipeline pipeline = pipelineFactory.extractPipeline("Piper", compileJob);

        assertEquals(pipeline,
                     new Pipeline("Piper",
                                  asList(new Stage("Build", asList(new Task("comp", "Compile", idle()),
                                                                   new Task("anal", "Analyze", idle()))),
                                         new Stage("Test", asList(new Task("test", "Test", idle()))))));
    }

    private AbstractProject createMockJob(String name, String displayName, String stageName)
    {
        PipelineProperty property = displayName != null || stageName != null
                                    ? new PipelineProperty(nullToEmpty(displayName), nullToEmpty(stageName))
                                    : null;

        AbstractProject project = Mockito.mock(AbstractProject.class);
        when(project.getProperty(PipelineProperty.class)).thenReturn(property);
        when(project.getName()).thenReturn(name);
        when(project.getDisplayName()).thenReturn(displayName);
        return project;
    }
}
