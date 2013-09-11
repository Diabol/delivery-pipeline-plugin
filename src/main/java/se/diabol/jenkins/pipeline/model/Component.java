package se.diabol.jenkins.pipeline.model;

import com.google.common.collect.ImmutableList;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean(defaultVisibility = 100)
public class Component extends AbstractItem
{
    private final List<Pipeline> pipelines;

    public Component(String name, List<Pipeline> pipelines)
    {
        super(name);
        this.pipelines = ImmutableList.copyOf(pipelines);
    }

    @Exported
    public List<Pipeline> getPipelines()
    {
        return pipelines;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this).add("name", getName()).add("pipelines", pipelines).toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), pipelines);
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || o instanceof Component && equals((Component)o);
    }

    private boolean equals(Component o)
    {
        return super.equals(o) && Objects.equals(pipelines, o.pipelines);
    }
}
