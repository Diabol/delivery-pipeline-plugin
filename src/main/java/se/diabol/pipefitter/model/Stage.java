package se.diabol.pipefitter.model;

import se.diabol.pipefitter.model.status.StatusFactory;
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

    private Stage()
    {
        super(null, null);
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
