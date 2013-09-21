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
public class Pipeline extends AbstractItem
{
    private List<Stage> stages;

    private String version;

    private String triggeredBy;

    private boolean aggregated;

    private String timestamp;

    public Pipeline(String name, String version, String timestamp,  String triggeredBy, List<Stage> stages, boolean aggregated)
    {
        super(name);
        this.version = version;
        this.triggeredBy = triggeredBy;
        this.aggregated = aggregated;
        this.stages = ImmutableList.copyOf(stages);
        this.timestamp = timestamp;
    }

    @Exported
    public List<Stage> getStages()
    {
        return stages;
    }

    @Exported
    public String getVersion()
    {
        return version;
    }

    @Exported
    @SuppressWarnings("unused")
    public String getTimestamp() {
        return timestamp;
    }

    @Exported
    @SuppressWarnings("unused")
    public boolean isAggregated()
    {
        return aggregated;
    }

    @Exported
    @SuppressWarnings("unused")
    public String getTriggeredBy() {
        return triggeredBy;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), stages);
    }

    @Override
    public boolean equals(Object o)
    {
        return o == this || o instanceof Pipeline && equals((Pipeline) o);
    }

    private boolean equals(Pipeline o)
    {
        return super.equals(o) && Objects.equals(stages, o.stages) && Objects.equals(version, o.version);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("name", getName())
                .add("version", getVersion())
                .add("stages", getStages())
                .toString();
    }
}
