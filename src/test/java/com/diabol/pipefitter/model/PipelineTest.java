package com.diabol.pipefitter.model;

import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

import static com.diabol.pipefitter.model.status.StatusFactory.success;
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
                                         asList(new Stage("Build",
                                                          asList(new Task("Compile", success())))));

        XmlMarshaller.getMarshaller().marshal(pipeline, buffer);
        Pipeline pipelineCopy = XmlMarshaller.getUnmarshaller()
                                             .unmarshal(new StreamSource(new StringReader(buffer.toString())),
                                                        Pipeline.class).getValue();
        assertEquals(pipelineCopy, pipeline);
    }
}
