package se.diabol.pipefitter.model;

import se.diabol.pipefitter.model.status.Status;
import se.diabol.pipefitter.model.status.StatusFactory;
import com.google.common.collect.ImmutableList;
import se.diabol.pipefitter.model.status.StatusFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@XmlRootElement
public class Pipeline extends Component
{
    @XmlElement(name = "stage")
    private List<Stage> stages;

    private Pipeline()
    {
        super(null, null);
    }

    public Pipeline(String name, List<Stage> stages)
    {
        super(name, StatusFactory.idle()); // todo: IDLE is cheating
        this.stages = ImmutableList.copyOf(stages);
    }

    public List<Stage> getStages()
    {
        return stages;
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
        return super.equals(o) && Objects.equals(stages, o.stages);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("name", getName())
                .add("status", getStatus())
                .add("stages", getStages())
                .toString();
    }
}
