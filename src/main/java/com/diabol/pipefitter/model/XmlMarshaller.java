package com.diabol.pipefitter.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class XmlMarshaller
{
    private final static JAXBContext context;
    static {
        try {
            context = JAXBContext.newInstance(Pipeline.class);
        }
        catch(JAXBException e) {
            throw new ExceptionInInitializerError("Unable to create JAXBContext for pipeline model.");
        }
    }

    public static Marshaller getMarshaller() throws JAXBException
    {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        return marshaller;
    }

    static Unmarshaller getUnmarshaller() throws JAXBException
    {
        return context.createUnmarshaller();
    }
}
