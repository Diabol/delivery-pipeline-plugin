package se.diabol.pipefitter.model;

import org.testng.annotations.Test;
import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.Stage;
import se.diabol.pipefitter.model.Task;
import se.diabol.pipefitter.model.XmlMarshaller;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

import static se.diabol.pipefitter.model.status.StatusFactory.success;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineTest
{
    @Test
    public void testXmlMarshalling() throws Exception
    {
        StringWriter buffer = new StringWriter();

        Pipeline pipeline = new Pipeline("Test pipeline",
                                         "1.0", asList(new Stage("Build",
                                                          asList(new Task("comp", "Compile", success(), "link")))));

        XmlMarshaller.getMarshaller().marshal(pipeline, buffer);
        String xml = buffer.toString();
        Pipeline pipelineCopy
                = XmlMarshaller.getUnmarshaller()
                               .unmarshal(new StreamSource(new StringReader(xml)), Pipeline.class)
                               .getValue();
        assertEquals(pipelineCopy, pipeline);
    }
}
