package se.diabol.jenkins.pipeline.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * This is the common abstraction for all the entities that makes a pipeline.
 *
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean(defaultVisibility = 100)
public abstract class AbstractItem
{
    private final String name;

    protected AbstractItem(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this).add("name", name).toString();
    }

    @Exported
    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || o instanceof AbstractItem && equals((AbstractItem) o);
    }

    private boolean equals(AbstractItem o)
    {
        return Objects.equals(name, o.name);
    }
}
