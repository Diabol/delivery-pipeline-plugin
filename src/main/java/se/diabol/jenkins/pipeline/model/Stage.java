package se.diabol.jenkins.pipeline.model;

import com.google.common.collect.ImmutableList;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.model.status.StatusFactory;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean(defaultVisibility = 100)
public class Stage extends Component
{
    private List<Task> tasks;

    private String version;

    private Stage()
    {
        super(null, null);
    }

    public Stage(String name, List<Task> tasks, String version) {
        super(name, StatusFactory.idle()); // todo: IDLE is cheating
        this.tasks = tasks;
        this.version = version;
    }

    public Stage(String name, List<Task> tasks)
    {
        super(name, StatusFactory.idle()); // todo: IDLE is cheating
        this.tasks = ImmutableList.copyOf(tasks);
    }

    @Exported
    public List<Task> getTasks()
    {
        return tasks;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(tasks);
    }

    @Override
    public boolean equals(Object o)
    {
        return o == this || o instanceof Stage && equals((Stage) o);
    }

    private boolean equals(Stage o)
    {
        return Objects.equals(tasks, o.tasks) && Objects.equals(version, o.version) && super.equals(o) ;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("name", getName())
                .add("version", getVersion())
                .add("status", getStatus())
                .add("tasks", getTasks())
                .toString();
    }
}
