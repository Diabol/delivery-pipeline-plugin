package se.diabol.pipefitter.model;

import com.google.common.collect.ImmutableList;
import se.diabol.pipefitter.model.status.StatusFactory;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Objects;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Stage extends Component
{
    @XmlElement(name = "task")
    private List<Task> tasks;

    private String version = null;

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

    public List<Task> getTasks()
    {
        return tasks;
    }

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
        return Objects.equals(tasks, o.tasks) && super.equals(o);
    }
}
