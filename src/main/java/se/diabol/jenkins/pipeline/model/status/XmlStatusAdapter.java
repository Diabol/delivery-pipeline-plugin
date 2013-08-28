package se.diabol.jenkins.pipeline.model.status;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class XmlStatusAdapter extends XmlAdapter<String, Status>
{
    @Override
    public Status unmarshal(String v) throws Exception
    {
        return v != null? StatusFactory.valueOf(v): null;
    }

    @Override
    public String marshal(Status v) throws Exception
    {
        return v != null? v.toString(): null;
    }
}
