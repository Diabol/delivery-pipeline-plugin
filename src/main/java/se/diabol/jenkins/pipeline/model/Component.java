package se.diabol.jenkins.pipeline.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.model.status.Status;

import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * This is the common abstraction for all the entities that makes a pipeline.
 *
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean(defaultVisibility = 100)
public abstract class Component
{
    private final String name;

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

    @Exported
    public String getName()
    {
        return name;
    }

    @Exported
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
