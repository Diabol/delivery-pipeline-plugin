package com.diabol.pipefitter.model;

import com.diabol.pipefitter.model.status.Status;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import static com.google.common.base.Objects.toStringHelper;

/**
 * This is the common abstraction for all the entities that makes a pipeline.
 *
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public abstract class Component
{
    @XmlAttribute
    private final String name;

    @XmlAttribute(required = false)
    private final Status status;

    protected Component(String name, Status status)
    {
        this.name = name;
        this.status = status;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this).add("name", name).add("status", status).toString();
    }

    public String getName()
    {
        return name;
    }

    public Status getStatus()
    {
        return status;
    }
}
