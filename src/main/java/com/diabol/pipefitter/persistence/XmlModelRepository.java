package com.diabol.pipefitter.persistence;

import com.diabol.pipefitter.dashboard.View;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class XmlModelRepository
{
    private final static ThreadLocal<JAXBContext> JAXB_CONTEXT = new ThreadLocal<JAXBContext>()
    {
        @Override protected JAXBContext initialValue()
        {
            try
            {
                return JAXBContext.newInstance(View.class);
            }
            catch (JAXBException e)
            {
                throw new RuntimeException(e);
            }
        }
    };

    public View loadModel(File file) throws IOException
    {
        return null;
    }
}
