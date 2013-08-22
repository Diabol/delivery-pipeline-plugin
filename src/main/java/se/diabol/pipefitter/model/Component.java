package se.diabol.pipefitter.model;

import se.diabol.pipefitter.model.status.Status;
import se.diabol.pipefitter.model.status.Status;

import javax.xml.bind.annotation.XmlAttribute;

import java.util.Objects;

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

    @Override
    public int hashCode()
    {
        return Objects.hash(name, status);
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || o instanceof Component && equals((Component) o);
    }

    private boolean equals(Component o)
    {
        return Objects.equals(name, o.name) && Objects.equals(status, o.status);
    }
}
